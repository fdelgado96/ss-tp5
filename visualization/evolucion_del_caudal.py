#Asume un archivo de entrada con el tiempo de salida de cada particula
import numpy as np
import matplotlib.pyplot as plt
import array

PATHS_TO_MEASUREMENTS = [
    'first/path',
    'second/path',
]
NUMBER_OF_WINDOWS = 50
WINDOW_SIZE = 50

def get_sliding_window_measures(path):
    times = np.genfromtxt(path)
    total_windows = times.size - WINDOW_SIZE + 1
    if total_windows < NUMBER_OF_WINDOWS:
        raise ValueError(
            'the ammount of particles measured exceded the possible ammount'
        )

    sliding_window_measures = np.zeros(total_windows)

    #convertir de tiempo acumulado a diferencias
    times = [y - x for x,y in zip(times, times[1:])]
    times = np.array(times)

    #iterar el sliding window
    for i in range(0, NUMBER_OF_WINDOWS):
        sliding_window_measures[i] = np.average(times[i:i+WINDOW_SIZE])

    return sliding_window_measures


total_measures = []
for path in PATHS_TO_MEASUREMENTS:
    total_measures.append(get_sliding_window_measures(path))

measures_matrix = np.vstack(total_measures)
measures_deviations = np.std(measures_matrix, axis=0)
measures_means = np.mean(measures_matrix, axis=0)

plt.errorbar(array.array('i', (0 for i in range(WINDOW_SIZE, WINDOW_SIZE+NUMBER_OF_WINDOWS))),
             measures_means, fmt='bo', markersize=3, yerr=measures_deviations,
             label='Fracción de particulas en la sección derecha ' + r'$\mathit{dt}$ = $2\mathrm{e}{-4}$')

plt.ylabel('Caudal en partículas por segundo')
plt.xlabel('Número de partícula')