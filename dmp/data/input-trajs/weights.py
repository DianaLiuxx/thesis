import numpy as np
from scipy.optimize import minimize


def read_data(file_path):
    data = np.loadtxt(file_path, skiprows=1)  # skip the header row
    time = data[:, 0]
    Q = data[:, 1]
    Qd = data[:, 2]
    Qdd = data[:, 3]
    return time, Q, Qd, Qdd

file_path = "D:\\lxx\\thesis\\cases\\lxx\\dmp\\data\\input-trajs\\path1.traj"

# Read the data from the file
time, Q, Qd, Qdd = read_data(file_path)

# print("time =", time)
# print("Q =", Q)
# print("Qd =", Qd)
# print("Qdd =", Qdd)


# Parameters for the problem
n = 15
h = 0.65 * (1.0 / (n - 1.0)) * (1.0 / (n - 1.0))
c = np.linspace(0, 1, n)  # Centers for basis functions

g = 1.0
x0 = 0.0
D = 25.0
K = 25.0 * 25 / 4
tau = 0.001


# Define the target function
def f_target(s, g, x, D, v, K, tau, vp):
    return -K * (g - x) + D * v + tau * vp

# Define the basis functions
def basis_functions(s, c, h):
    return np.exp(-0.5 * ((s - c) ** 2) / h)

def f_approx(s, w, c, h):
    psi = basis_functions(s, c, h)
    numerator = np.dot(w, psi)
    denominator = np.sum(psi)
    return (numerator / denominator) * s


# Define the error criterion
def error_criterion(w, time, Q, Qd, Qdd, h, c, g, x0, D, K, tau):
    error = 0.0
    for i in range(len(time)):
        s = time[i]
        x = Q[i]
        v = Qd[i]
        vp = Qdd[i]
        f_t = f_target(s, g, x, D, v, K, tau, vp)
        f_a = f_approx(s, w, h, c)
        error += (f_t - f_a)**2
    return error

# Optimization to find the weights
initial_w = np.random.rand(n)



result = minimize(error_criterion, initial_w, args=(time, Q, Qd, Qdd, h, c, g, x0, D, K, tau))

# Optimized weights
optimal_w = result.x

print("Optimized weights:", optimal_w)
