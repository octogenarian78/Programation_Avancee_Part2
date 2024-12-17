import subprocess
import os
from Cython import long


def run_java_program(java_file, num_throws, num_workers, name_file):
    # Compiler le fichier Java
    compile_command = ["javac", java_file]
    print(f"Compilation de {java_file}...")
    try:
        subprocess.run(compile_command, check=True)
        print("Compilation réussie.")
    except subprocess.CalledProcessError as e:
        print("Erreur lors de la compilation :", e)
        return None

    # Extraire le nom de la classe principale (en supposant qu'il correspond au nom du fichier sans l'extension .java)
    class_name = os.path.splitext(java_file)[0]

    # Stocker les résultats
    results = []

    # Exécuter le programme Java 5 fois pour le nombre de workers donné
    for i in range(15):
        run_command = ["java", class_name, str(num_throws), str(num_workers), name_file]
        print(f"Exécution du programme Java (tentative {i + 1}, workers : {num_workers})...")
        try:
            result = subprocess.run(run_command, capture_output=True, text=True, check=True)
            print("Sortie :")
            print(result.stdout)
            results.append(result.stdout.strip())  # Sauvegarder la sortie
        except subprocess.CalledProcessError as e:
            print("Erreur lors de l'exécution :", e)
            if e.stdout:
                print("Sortie standard :")
                print(e.stdout)
            if e.stderr:
                print("Sortie des erreurs :")
                print(e.stderr)
    return results

if __name__ == "__main__":
    java_file = "Assignment102.java"  # Remplacez par le nom de votre fichier Java
    num_throws = 1000000# Nombre de lancers pour la simulation Monte Carlo
    max_worker = 16 # Nombre maximum de Worker
    results_by_workers = {}

    # # Tester le programme avec un nombre de workers allant de 1 à 16
    # for num_workers in range(1, max_worker+1):  # De 1 à 16 workers
    #     print(f"\n=== Test de {java_file} avec {num_workers} workers ===")
    #     results = run_java_program(java_file, long(num_throws/num_workers), num_workers, "Pi_forte")
    #     if results:
    #         results_by_workers[num_workers] = results
    #
    # # Résumé des résultats
    # print("\n=== Résumé des résultats ===")
    # for workers, results in results_by_workers.items():
    #     print(f"Workers : {workers}")
    #     for i, output in enumerate(results):
    #         print(f"  Exécution {i + 1} : {output}")


    while num_throws <= 10000000:
      run_java_program(java_file, long(num_throws*max_worker), max_worker, "error_Assigment102")
      num_throws += 1000000
