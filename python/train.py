import socket
import logging
import random
import numpy as np
import matplotlib.pyplot as plt

HOST = 'localhost'
PORT = 7627

# income: Low   - 0, High   - 1
# gender: Male  - 0, Female - 1
# age:    Young - 0, Old    - 1
LMY = (0, 0, 0)
LMO = (0, 0, 1)
LFY = (0, 1, 0)
LFO = (0, 1, 1)
HMY = (1, 0, 0)
HMO = (1, 0, 1)
HFY = (1, 1, 0)
HFO = (1, 1, 1)

LM = [LMY, LMO]
LF = [LFY, LFO]
HM = [HMY, HMO]
HF = [HFY, HFO]
LY = [LMY, LFY]
LO = [LMO, LFO]
HY = [HMY, HFY]
HO = [HMO, HFO]
MY = [LMY, HMY]
MO = [LMO, HMO]
FY = [LFY, HFY]
FO = [LFO, HFO]

all_segments = [LMY, LMO, LFY, LFO, HMY, HMO, HFY, HFO]
meta_segments = [[LMY], [LMO], [LFY], [LFO], [HMY], [HMO], [HFY], [HFO], LM, LF, HM, HF, LY, LO, HY, HO, MY, MO, FY, FO]
proportions = {
    LMY: 1836,
    LMO: 1795,
    LFY: 1980,
    LFO: 2401,
    HMY: 517,
    HMO: 808,
    HFY: 256,
    HFO: 407,
}
deltas = [.4, .6, .8]

SUM_PROPORTIONS = sum(proportions.values())
NUM_BOTS = 7
NUM_IMPRESSIONS = 10000
NUM_DAYS = 10

TRIALS = 10000
# STEPS = 999
DISCOUNT = 0.99
ACTIONS = 8
THRESHOLD = 0.2

POLICY_INPUT_SIZE = 10
POLICY_HIDDEN_SIZE = 128
POLICY_OUTPUT_SIZE = ACTIONS
CRITIC_INPUT_SIZE = POLICY_OUTPUT_SIZE
CRITIC_HIDDEN_SIZE = 128
CRITIC_OUTPUT_SIZE = 1

DEV = 1e-10
LEARNING_RATE = 1e-3

def __sample():
  segments = meta_segments[random.randint(0, len(meta_segments) - 1)]
  reach = deltas[random.randint(0, len(deltas) - 1)] * sum([proportions[m] for m in segments])
  budget = reach
  return segments, reach, budget

def __user():
  rand = random.randint(1, SUM_PROPORTIONS)
  m = 0
  for s, v in proportions.items():
    m += v
    if m >= rand:
      return s

  assert (False)


def quality_score(num, reach):
  return 0.48950381445847413 * (np.arctan(4.08577 * num / reach - 3.08577) - np.arctan(-3.08577))

def second_price(bids):
  if len(bids) is 0:
    return None, 0

  winning_bid = None
  price = -1
  for b in bids:
    bot = b.bot
    if b.price > bot.budget or b.price > b.limit:
      continue
    if winning_bid is None:
      winning_bid = b
      price = 0
    else:
      price = b.price
      break

  assert (winning_bid is None or (price >= 0 and price <= winning_bid.limit and price <= winning_bid.bot.budget))
  if winning_bid is not None:
    winning_bid.limit -= price
    winning_bid.bot.budget -= price
    return winning_bid.bot, price
  else:
      return None, 0

def print_all_bids(all_bids):
  print('-------- ALL BIDS --------')
  for segment, bids in all_bids.items():
    print('Segment %s' % (segment,))
    for bid in bids:
      print(bid)
  print('-------- ALL BIDS --------')


def print_wins(wins):
  print('-------- WINS --------')
  for bot, num in wins.items():
    print('Bot %d: won %d' % (bot.id, num))
  print('-------- WINS --------')


class bid:

  def __init__(self, segment, price, limit, bot):
    self.segment = segment
    self.price = price
    self.limit = limit
    self.bot = bot

  def __str__(self):
    return "[bid: segment: %s, price: %f, limit: %f, bot: %d]" % (self.segment, self.price, self.limit, self.bot.id)


class abstract_bot:

  def __init__(self, id):
    self.id = id

  def new_day(self, segments, reach, budget):
    self.name = 'NULL'
    self.segments = segments
    self.reach = reach
    self.budget = budget

  def get_bundle(self, day):
    raise Exception('Not Yet Implemented')

  def report(self, day, win, profit):
    return False

  def end_round(self, profit):
    pass

  def end_game(self):
    pass

  def __str__(self):
    return "[%s: id: %d, segments: %s, reach: %f, budget: %f]" % (self.name, self.id, self.segments, self.reach, self.budget)

class tier_one_bot (abstract_bot):

  def __init__(self, id):
    super().__init__(id)
    self.name = 'tier_one_bot'


  def get_bundle(self, day):
    ret = []
    for s in self.segments:
      ret.append(bid(s, (random.random() + .2) * self.budget / self.reach, self.budget, self))
    return ret

class constant_bot (abstract_bot):

  def __init__(self, id):
    super().__init__(id)
    self.name = 'constant_bot'

  def get_bundle(self, day):
    total = sum([proportions[s] for s in self.segments])
    ret = []
    for s in self.segments:
      virtual_budget = self.budget * proportions[s] / total
      virtual_reach = self.reach * proportions[s] / total
      price = virtual_budget / virtual_reach
      if price > 1:
        price *= .9
      ret.append(bid(s, price, virtual_budget * .95, self))
    return ret

  def __str__(self):
    return "[constant_bot: id: %d, segments: %s, reach: %f, budget: %f]" % (self.id, self.segments, self.reach, self.budget)


class learning_bot (abstract_bot):
  def reset(self):
    self.st, self.act, self.v = None, None, None
    self.rs, self.sts, self.acts, self.vs = [], [], [], []

  def __init__(self, id):
    super().__init__(id)

    import tensorflow as tf

    self.name = 'learning_bot'

    # state = market segments (0 & 1 each) + reach + budget
    state = tf.placeholder(shape=[None, POLICY_INPUT_SIZE], dtype=tf.float32)
    w_1 = tf.Variable(tf.random_normal([POLICY_INPUT_SIZE, POLICY_HIDDEN_SIZE], dtype=tf.float32, stddev=DEV))
    b_1 = tf.Variable(tf.random_normal([POLICY_HIDDEN_SIZE], dtype=tf.float32, stddev=DEV))
    w_2 = tf.Variable(tf.random_normal([POLICY_HIDDEN_SIZE, POLICY_OUTPUT_SIZE], dtype=tf.float32, stddev=DEV))
    b_2 = tf.Variable(tf.random_normal([POLICY_OUTPUT_SIZE], dtype=tf.float32, stddev=DEV))
    c_1 = tf.Variable(tf.random_normal([CRITIC_INPUT_SIZE, CRITIC_HIDDEN_SIZE], dtype=tf.float32, stddev=DEV))
    a_1 = tf.Variable(tf.random_normal([CRITIC_HIDDEN_SIZE], dtype=tf.float32, stddev=DEV))
    c_2 = tf.Variable(tf.random_normal([CRITIC_HIDDEN_SIZE, CRITIC_OUTPUT_SIZE], dtype=tf.float32, stddev=DEV))
    a_2 = tf.Variable(tf.random_normal([CRITIC_OUTPUT_SIZE], dtype=tf.float32, stddev=DEV))

    hidden = tf.nn.relu(tf.matmul(state, w_1) + b_1)
    output = tf.nn.softmax(tf.matmul(hidden, w_2) + b_2)
    critic_output = tf.matmul(tf.nn.relu(tf.matmul(output, c_1) + a_1), c_2) + a_2

    rewards = tf.placeholder(shape=[None], dtype=tf.float32)
    actions = tf.placeholder(shape=[None], dtype=tf.int32)
    indices = tf.range(0, tf.shape(output)[0]) * POLICY_OUTPUT_SIZE + actions
    act_probs = tf.gather(tf.reshape(output, [-1]), indices)

    loss = -tf.reduce_mean(tf.log(act_probs) * rewards)
    critic_loss = tf.reduce_mean(tf.square(rewards))
    train_op = tf.train.AdamOptimizer(learning_rate=LEARNING_RATE).minimize(loss + critic_loss)

    self.sess = tf.Session()
    self.sess.run(tf.global_variables_initializer())

    self.state = state
    self.output = output
    self.critic_output = critic_output
    self.rewards = rewards
    self.actions = actions
    self.act_probs = act_probs
    self.train_op = train_op
    self.action_space = [0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3]

    assert(len(self.action_space) == ACTIONS)
    self.total_rewards = []
    self.xs = []
    self.ranks = []
    self.reset()

  def __get_state(self):
    return [proportions[s] if s in self.segments else 0 for s in all_segments] + [self.reach, self.budget]

  def get_bundle(self, day):
    self.st = self.__get_state()

    act_dist, self.v = self.sess.run([self.output, self.critic_output], feed_dict={self.state: [self.st]})
    self.act = np.random.choice(self.action_space, p=act_dist[0])

    return [bid(seg, self.act, self.budget, self) for seg in self.segments]

  def report(self, day, win, profit):
    assert (self.st is not None)
    assert (self.act is not None)
    assert (self.v is not None)

    score = quality_score(win, self.reach)

    if (score <= THRESHOLD):
      r = profit
    else:
      r = -self.reach
    self.rs.append(r)
    self.sts.append(self.st)
    self.acts.append(self.act)
    self.vs.append(float(self.v))

    return score <= THRESHOLD

  def end_round(self, profit):
    d, ds = 0, []
    self.total_rewards.append(profit)

    if len(self.total_rewards) == 100:
      self.ranks.append(sum(self.total_rewards) / 100)
      self.xs.append(len(self.ranks))
      self.total_rewards = []
      print(len(self.xs))

    for r, v in zip(self.rs[::-1], self.vs[::-1]):
        d = DISCOUNT * d + r
        ds.insert(0, d - v)

    self.sess.run(self.train_op, feed_dict={self.state: self.sts, self.rewards: ds, self.actions: self.acts})
    self.reset()

  def end_game(self):
    plt.plot(self.xs, self.ranks)
    plt.show()
    self.sess.close()


if __name__ == '__main__':
  # initializes bots
  bots = []
  for i in range(NUM_BOTS):
    bot = tier_one_bot(i)
    bots.append(bot)
  bots.append(learning_bot(NUM_BOTS))

  for _ in range(TRIALS):
    profits = {bot: 0 for bot in bots}

    init_budgets = {}
    for day in range(1, NUM_DAYS + 1):
      for bot in bots:
        segments, reach, budget = __sample()

        if day > 1:
          # discounts by quality score
          budget *= quality_score(wins[bot], bot.reach)

        # sets new campaign
        bot.new_day(segments, reach, budget)
        init_budgets[bot] = budget

      # gathers bids
      all_bids = {s: [] for s in all_segments}
      for bot in bots:
        bundle = bot.get_bundle(day)
        for b in bundle:
          assert (b.limit <= bot.budget)
          all_bids[b.segment].append(b)

      # sorts bids in each market
      for s, b in all_bids.items():
        all_bids[s] = sorted(b, key=lambda x: x.price, reverse=True)
      # print_all_bids(all_bids)

      # generates random users
      wins = {bot: 0 for bot in bots}
      spendings = {bot: 0 for bot in bots}
      for _ in range(NUM_IMPRESSIONS):
        u = __user()
        winner, price = second_price(all_bids[u])
        if winner != None:
          assert (winner in wins)
          wins[winner] += 1
          spendings[winner] += price

      # calculates profits
      day_profits = {}
      for bot in bots:
        day_profits[bot] = min(1, 0 if bot.reach == 0 else wins[bot] / bot.reach) * init_budgets[bot] - spendings[bot]
        profits[bot] += day_profits[bot]

      # sets end of day
      calls_end = False
      for bot in bots:
        ret = bot.report(day, wins[bot], day_profits[bot])
        calls_end = ret or calls_end

      if calls_end:
        break

    # print_wins(wins)
    val = profits[bots[NUM_BOTS]]
    rank = 1
    for _, p in profits.items():
      if p < val:
        rank += 1
    bots[NUM_BOTS].end_round(rank)

  for bot in bots:
    bot.end_game()






"""
def test():
  logger = logging.getLogger('client_logger')
  logging.basicConfig(level=logging.DEBUG, format='%(message)s')

  client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  client.connect(("localhost", PORT))

  try:
    logger.info('[+] Connected to %d' % PORT)

    while True:
      message = client.recv(PORT).decode('UTF-8')[:-1]
      logger.debug('[+] Server sent \"%s\"' % message)
      client.send(bytearray('message \"%s\" processed..\n' % message, 'UTF-8'))

  finally:
    client.close()
"""
