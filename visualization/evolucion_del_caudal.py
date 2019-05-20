#Asume un archivo de entrada con el tiempo de salida de cada particula
import numpy as np
import matplotlib.pyplot as plt

PATHS_TO_MEASUREMENTS = [
    '../data/0.15_70.0_5e-5_exitTimes.csv',
    '../data/0.2_70.0_5e-5_exitTimes.csv'
]
Labels = [
    'D = 15cm',
    'D = 20cm'
]

NUMBER_OF_WINDOWS = 50
WINDOW_SIZE = 50

def get_sliding_window_measures(path):
    times = np.genfromtxt(path)
    total_windows = times.size - WINDOW_SIZE + 1
    if total_windows < NUMBER_OF_WINDOWS:
        raise ValueError('the amount of particles measured is smaller than the minimum required')

    sliding_window_means = np.zeros(total_windows)
    sliding_window_stds = np.zeros(total_windows)

    #convertir de tiempo acumulado a diferencias
    deltas = np.diff(times)

    #iterar el sliding window
    for i in range(0, NUMBER_OF_WINDOWS):
        sliding_window_means[i] = np.mean(deltas[i:i+WINDOW_SIZE-1])
        sliding_window_stds[i] = np.std(deltas[i:i+WINDOW_SIZE-1])

    return times[WINDOW_SIZE-1:], sliding_window_means, sliding_window_stds


total_measures = []
for path in PATHS_TO_MEASUREMENTS:
    times, means, stds = get_sliding_window_measures(path)
    plt.errorbar(times, means, yerr=stds, fmt='bo', markersize=3, label='Holis')
    print('last mean: {:.2E}'.format(means[-1]))
    print('last std: {:.2E}'.format(stds[-1]))

plt.ylabel('Caudal en partículas por segundo')
plt.xlabel('Tiempo [s]')
plt.legend()