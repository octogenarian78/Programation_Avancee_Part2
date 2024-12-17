import matplotlib.pyplot as plt
import csv

# Listes pour stocker les données
iteration = []
numworker = []
temps = []
pi = []
error = []

# Variables temporaires pour calculs intermédiaires
tmp = 0
idr = 0
repeat_count = 5  # Nombre de répétitions par configuration

# Lecture du fichier CSV
with open('Socket_faible.csv', newline='') as csvfile:
    file = csv.reader(csvfile, delimiter=';')
    for row in file:
        if row[0] != 'totalIterations':  # Ignorer l'en-tête
            print(row)
            if len(row) >= 5:  # Vérifier que la ligne contient au moins 5 colonnes
                tmp += float(row[2].replace(',', '.'))  # Ajouter la durée au total temporaire
                if (idr % repeat_count) == (repeat_count - 1):
                    # Ajouter les données moyennes après chaque groupe de répétitions
                    if int(row[1])%2 == 0:
                        iteration.append(int(row[0]))
                        numworker.append(int(row[1]))
                        temps.append(tmp / repeat_count)  # Calculer la moyenne du temps
                        pi.append(float(row[3].replace(',', '.')))
                        error.append(float(row[4].replace(',', '.')))
                    tmp = 0  # Réinitialiser le total temporaire
                idr += 1

# Calculer le speedup (accélération relative)


spdu = []# Temps d'exécution avec 1 travailleur
for i in range(len(temps)):
    spdu.append((temps[0]/temps[i]))

# Afficher les données pour vérification
print("Iterations:", iteration)
print("Nombre de travailleurs:", numworker)
print("Temps:", temps)
print("Pi:", pi)
print("Erreur:", error)

# Générer le graphique
plt.plot(numworker, spdu, marker='o')
plt.plot(numworker,[1]*len(numworker))
plt.title("Courbe de scalabilité faible en mémoire distribuée")
plt.xlabel("Nombre de workers")
plt.ylabel("Speedup")
plt.grid(True)
plt.show()
