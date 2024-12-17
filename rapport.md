# Rapport Programmation Avancée partie 2


## Plan

- ### [I – Méthode de Monte-Carlo pour calculer Pi](#p1)
- ### [II - Algorithme et parallélisation](#p2)
  - #### [**a) - Itération parallèle**](#p2a)
  - #### [**b) - Master Worker**](#p2b)
- ### [III - Algorithme et parallélisation](#p3)
    - #### [**a) - Analyse de `Assignment102.java`**](#p3a)
    - #### [**b) - Analyse de `Pi.java`**](#p3b)
- ### [IV - Qualité de test de performance](#p4)
    - #### [**a) - Définition scalabilité forte et scalabilité faible**](#p4a)
    - #### [**b) Réalisation des tests de scalabilité sur `Assignment102.java`**](#p4b)
        - ##### [**1) - Modification du code `Assignment102.java`**](#p4b1)
        - ##### [**2) Création d'un script python qui lance automatiquement les codes java**](#p4b2)
        - #### [**3) Test de performance de `Assignment102.java`**](#p4b3)
- ### [V - Mise en oeuvre mémoire distribuée](#p5)
- ### [VI - Calcul de performance en mémoire distribuée](#p6)
- ### [VII - calcul d'erreur](#p7)
  <br><br><br>

---

## <a name="p1"></a> I - Méthode de Monte-Carlo pour calculer Pi :

La méthode de Monte-Carlo est une de probabilité qui permet d'estimer la valeur de Pi, elle fonctionne de la façon suivante : 

<br>

Soit $A_{1/4d}$  l'aire d'un quart de disque de rayon $1$ :  

$A_{1/4d} = \frac{\pi r^2}{4} = \frac{\pi}{4}$


Soit $A_c$ l'aire d'un carré de côté $1$ :  

$A_c = 1^2 = 1$

Soient les points  $X_p (x_p, y_p)$ dont les coordonnées sont tirées selon une loi  $M(]0;1[)$ .  

<img src=images_rapport\graphique_monte-carlo.png alt="Graphique représentant un tirage" style="width:30%;"> <br><small><small>*Graphique représentant une épreuve de Monte-Carlo*</small></small></img>

La probabilité que $X_p$ soit tiré dans le quart de disque est :  

$\boxed{P = \frac{A_{1/4d}}{A_c} = \frac{\pi}{4}}$

On effectue  $n_{\text{tot}}$ tirages aléatoires.  
Soit  $n_{\text{cible}}$ le nombre de points tirés dans le quart de disque.  

Si $n_{\text{tot}}$ est grand, alors on peut approximer $P$ par :  

$P = \frac{n_{\text{cible}}}{n_{\text{tot}}} \approx \frac{\pi}{4}$

D'où :  $\boxed{\pi \approx 4 \cdot \frac{n_{\text{cible}}}{n_{\text{tot}}}}$

<br>

---
## <a name="p2"></a> II - Algorithme et parallélisation :

On va maintenant chercher à automatiser la réalisation d'un grand nombre d'épreuves de Monte-Carlo à l'aide d'algorithmes.

### <a name="p2a"></a> a) - Itération parallèle :

On propose donc un algorithme simple en pseudo code
```
initialiser n_cible à 0

pour p allant de 0 à n_tot-1

    xp = valeur aléatoire en 0 et 1
    yp = valeur aléatoire en 0 et 1

    si (xp au carré + yp au carré) inférieur à 1
        ajouter 1 à n_cible
    fin si

fin pour

calculer pi = 4 * n_cible / n_tot
```

Cet algorithme réalise Monte-Carlo en séquentiel, on va chercher à paralléliser cet algorithme.


#### Indentification des taches :

pour pouvoir paralléliser cet algorithme on va chercher à identifier les taches réalisées, on a donc : 

* **T0** tirer et compter les ``n_tot`` points<br>
* **T1** calculer ``Pi``<br>

<br>

**T0** se décompose en ``n_tot`` sous-taches :<br>
* **T0p1** tirer Xp
* **T0p2** incrémenter ``n_cible``

#### Dépendance entre les taches :

* **T1** dépend de **T0**, impossible de calculer ``Pi`` sans ``n_cible``
* Les **T0p1** sont indépendant entre eux
* Pareil pour les **T0p2**
* **T0p2** dépend de **T0p1**

#### algorithme parallèle

```
fonction tirer_point()
    xp = valeur aléatoire en 0 et 1
    yp = valeur aléatoire en 0 et 1

    renvoyer booleen (xp au carré + yp au carré) inférieur à 1
fin fonction

initialiser n_cible à 0

pour p allant de 0 à n_tot-1

    si tirer_point()
        ajouter 1 à n_cible
    fin si

fin pour

calculer pi = 4 * n_cible / n_tot
```

`tirer_point()` peut être exécuter sur plusieurs *threads* en même temps car chacune de ses exécutions sont indépendantes les unes des autres.

#### Identifiaction de ressource critique :

Vu que les différents **T0p1** ne dépendent pas les uns des autres et que les différents **T0p2** ne dépendent pas non plus les uns des autres, ces deux sous-taches peuvent être parallélisées, c'est à dire être exécuter sur plusieurs *threads* en même temps, mais il faut faire attention à de possibles ressources critiques, des zones mémoires pour lesquelles plusieurs *threads* peuvent vouloir accéder en même temps.<br>

Dans notre cas la ressource critique serai ``n_cible`` que plusieurs *threads* pourrais vouloir modifier en même temps, il faut donc, pour cela, donc restreindre l'accés à la section critique ``ajouter 1 à n_cible``

### <a name="p2b"></a> b) - Master Worker :

Le paradigme **Master Worker**, consiste en un **Master** qui va diviser une grosse tache qu'il va diviser en plus petites taches qui vont être réparties sur différents **Workers** qui vont ensuite la réaliser et renvoyer leur résultat au **Worker** qui va pouvoir les traiter et les analyser.

<img src=images_rapport\diagramme_master_worker.png alt="diagramme fonctionnement Master Worker" style="width:50%;"> <br><small><small>*Diagramme reprsentant le fonctionnement du paradigme Master Worker*</small></small></img>

Ce paradigme peut être utiliser pour le calcul de Pi avec la méthode de Monte-Carlo avec par exemple le code suivant :

<br>

*pseudo code du Worker*<br>

```
Worker_MC : parametre(n_tot)
    initialiser n_cible à 0

    pour p allant de 0 à n_tot-1

        xp = valeur aléatoire en 0 et 1
        yp = valeur aléatoire en 0 et 1

        si (xp au carré + yp au carré) inférieur à 1
            ajouter 1 à n_cible
        fin si

    fin pour

renvoyer n_cible
```

<br>

*pseudo code du Master*<br>

```
Master_MC
    n_tot[nb_worker]
    initialiser n_CibleSomme à 0

    pour i allant de 0 à nb_worker-1
        n_tot[i] = n_total/nb_worker
        n_cible[i] = Worker_MC[i](n_tot[i]).start
    fin pour

    n_CibleSomme = somme de toutes les éléments n_cible
    pi = 4 * n_CibleSomme/n_total
```

Dans cette version, on a *Master_MC* qui va diviser une tache de taille ``n_total`` en ``nb_worker`` taches de taille ``n_tot``.<br>
On a aussi les *Worker_MC* qui vont réaliser ``n_tot`` épreuve de Monte-Carlo et vont renvoyer leurs résultats au *Master_MC* qui va les utiliser pour calculer Pi.

---
## <a name="p3"></a> III - Mise en oeuvre :

On va maintenant voir deux programme Java qui mettent en oeuvre les notions évoquées précédemment.

### <a name="p3a"></a> a) - Analyse de `Assignment102.java` :

Le programme java `Assignment102.java` calcule Pi avec la méthode de Monte-Carlo, il est composé de trois classe : `PiMonteCarlo`, `MonteCarlo` et `Assignment102`.<br>
Pour mieux comprendre les liens entre les classes on a réalisé un diagramme de classe en UML :

<img src=images_rapport\diagramme_UML_Assignment102.png alt="diagramme du code Assignment102.java" style="width:50%;"> <br><small><small>*Diagramme de classe d'Assignment102*</small></small></img>

Sur ce diagramme UML on peut voir que `Assignment102` qui dépend de `PiMonteCarlo`, on a `MonteCarlo` qui compose `PiMonteCarlo`, on a `MonteCarlo` qui réalise l'interface `Runnable`.<br>

`Assignment102.java` utilise le parallelisme iteratif comme proposé dans la partie [`II.a`](#p2a).<br>
Le problème de `Assignment102.java` est la ressource critique `nAtomSuccess` qui est de type `AtomicInteger`, ce qui veux dire que la valeur ne peut être manipulée que par une tache à la fois ce qui fait que dans le cas actuel si on verifie que que le point choisi aléatoirement est bien dans le quart de disque alors 75% de l'exécution se fait en itératif, une améloiration envisageable serai de verifier si le point choisi aléatoirement n'est pas dans le quart de disque comme ça seulement 25% de l'exécution serai en iteratif

### <a name="p3b"></a> b) - Analyse de `Pi.java` :

Le programme `Pi.java` calcule Pi avec la méthode de Monte-Carlo en utilisant le paradigme **Master Worker**, il est composé de trois classes : `Pi`, `Master`et `Worker`.<br>
Afin de mieux se représenter ce programme on va réaliser un diagramme de classe en UML : 

<img src=images_rapport\diagramme_UML_Pi.png alt="diagramme du code Pi.java" style="width:50%;"> <br><small><small>*Diagramme de classe de Pi*</small></small></img>

Comme dit précédemment `Pi.java` utilise le paradigme de programmation **Master Worker**, comme proposé dans la partie [`II.b`](#p2b).<br>
Avec l'utilisation du paradigme de programmation **Master Worker** on évite le problème de la ressource critique, car chaques taches possèdent leur propre compteur, donc aucune ressource critique.

---
## <a name="p4"></a> IV - Qualité de test de performance :

Cette partie va traiter de la mise en place et de la réalisation de tests de performance comme vu dans le cours de qualité de développement.<br>
Tous les programme et les différentes informations dans cette partie se trouverons sur la branche [test_de_scalabilite](https://github.com/octogenarian78/Programation_Avancee_Part2/tree/test_de_scalabilite) du github.<br>

L'ordinateur sur lequel vont être réalisées les test possède les specification suivantes : 

 * AMD Ryzen 7 5700U
 * 8 coeurs physiques
 * 16 coeurs logiques

<br>

On va étudier la performance des deux programme java analysés précédemment, pour on va étudier la **scalabilité forte** et **faible**.<br>

### <a name="p4a"></a> a) - Définition **scalabilité forte** et **scalabilité faible** :

Avant de continuer on va définir les termes de **scalabilité forte** et de **scalabilité faible** :
- **scalabilité forte** : On fixe une taille de problème et on augmente le nombre de processus.
- **scalabilité faible** : On fixe la taille du problème par processus et on augmente le nombre de
processus, la taille du problème augmente proportionnellement au nombre de processus.

### <a name="p4b"></a>  b) - Réalisation des tests de scalabilité sur `Assignment102.java` : 


#### <a name="p4b1"></a>1) - Modification du code `Assignment102.java` :
Avant de pourvoir réaliser les test sur `Assignment102.java`, il va faloir faire deux chose : modifier le code de `Assignment102.java` pour pouvoir le lancer en changeant ses parametre avec une commande et créer un programme python qui nous lance autotiquement des exécution de `Assignment102.java` en modifiant ses parametre.<br>

Pour faire en sorte que `Assignment102.java` soit lanceable  depuis une ligne de commande il faut modifier la fonction `main()` de la classe `Assignment102` : 

```java
public class Assignment102 {
    public static void main(String[] args) {
        PiMonteCarlo PiVal = new PiMonteCarlo(100000);
        long startTime = System.currentTimeMillis();
        double value = PiVal.getPi();
        long stopTime = System.currentTimeMillis();
```
dans la version ci-dessus qui est la version originale de la fonction `main()` de la classe `Assignment102` on peut voir que tous les parametre utilisés par le programme y sont directement définis ce qui nous empêche de les modfier si on cherche à lancer le programme via une ligne de commande, alors que dans le programme ci-dessous on utilise le parametre de `String[] args` qui est une liste de chaines de caractères qui peut être entrer en paramètre de la fonction. Le premier élément de la liste `args` va être le nombre d'iteration à réaliser, c'est à dire la taille de notre problème, le second argument est le nombre de worker, c'est à dire le nombre de processus que l'on va utiliser.

```java
public class Assignment102 {
    public static void main(String[] args) {
        long numIteration = Long.parseLong(args[0]);
        PiMonteCarlo PiVal = new PiMonteCarlo(numIteration);
        int numworker = Integer.parseInt(args[1]);
        long startTime = System.currentTimeMillis();
        double value = PiVal.getPi();
        long stopTime = System.currentTimeMillis();
```

pour pouvoir stocker les différentes données liées à l'exécution de ce programme on va créer une classe java qui va enregistrer dans un fichier csv : 

```java
    WriteToFile writeToFile = new WriteToFile();
        writeToFile.write(numIteration,numworker,(stopTime - startTime),value,(value - Math.PI),args[2]);
```
ce bout de code va être ajouter à la fin de la fonction `main()` de la classe `Assignment102` et va prendre en parametre la taille du problème, le nombre de processus utilisé, le temps de réalisation du problème, la valeur estimé de pi, la différence de cette valeur par rapport à la vrai valeur de pi et le troisième élément de la liste `args` qui sera le nom du fichier csv.


#### <a name="p4b2"></a>2) - Création d'un script python qui lance automatiquement les codes java :
Une fois toutes ces modifications faites, on peut passer à la création d'un fichier python qui va nous lancer automatiquement `Assignment102.java` en changeant les paramètres de la méthode `main()`.<br>
Pour l'automatisation de l'exécution on va utiliser les bibliothèques python `subprocess`, pour compiler le fichier java et pour exécuterla commande qui lance le code java, `os`, utilisé pour récupérer le nom de la classe principale de notre programme java par exemple dans `Assignment102.java` la classe principale est la classe `Assignment102`, et `Cython`, pour pouvoir créer des valeur de type `long`.

**Fonctionement du fichier python :**

 1) On donne les information importante au script python
 ```python
 if __name__ == "__main__":
    java_file = "Assignment102.java"  # Remplacez par le nom de votre fichier Java
    num_throws = 1_000_000# Nombre de lancers pour la simulation Monte Carlo
    max_worker = 16 # Nombre maximum de Worker
 ```
 2) ensuite on lance l'exécution des tests

```python
    for num_workers in range(1, max_worker+1):  # De 1 à 16 workers
        print(f"\n=== Test de {java_file} avec {num_workers} workers ===")
        results = run_java_program(java_file, long(num_throws * num_workers), num_workers, "Assignment102_faible")
        if results:
            results_by_workers[num_workers] = results
```

3) puis dans la fonction `run_java_program` on compile le code java
```python
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
```

4) enfin on lance la commande et on démarre les tests
```python
# Extraire le nom de la classe principale (en supposant qu'il correspond au nom du fichier sans l'extension .java)
    class_name = os.path.splitext(java_file)[0]

    # Stocker les résultats
    results = []

    # Exécuter le programme Java 5 fois pour le nombre de workers donné
    for i in range(5):
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
```
#### <a name="p4b3"></a> 3) Test de performance de `Assignment102.java` :

**Scalabilité forte :**<br>
Comme dit plus tôt la scalabilité forte consiste en un problème de taille fixe pour lequel on va augmenter le nombre de processus, pour éviter qu'il n'y ait pas toujours exactement la même portion de taile de problème pour les tests en fonction du nombre de processus on va choisir un nombre qui est multiple de tous les nombre infèrieur ou égaux à dans notre cas, 16. Si on veux trouver ce nombre, il nous faut trouver le PPCM (plus petit commun multiple) des nombre infèrieur ou égaux à 16, et donc après quelque calcule on trouve que le PPCM de ces nombres est 720720.

voici le tableau des valeur utilisé pour l'experience de scalabilité forte de `Assignment102.java`:

| Nombre de processus | taille du problème | taille du problème pour chaque processus |
| -- | -- | --|
| 1 | 7207200 | 7207200 |
| 2 | 7207200 | 3603600 |
| 3 | 7207200 | 2402400 |
| 4 | 7207200 | 1801800 |
| 5 | 7207200 | 1441440 |
| 6 | 7207200 | 1201200 |
| 7 | 7207200 | 1029600 |
| 8 | 7207200 | 900900 |
| 9 | 7207200 | 800800 |
| 10 | 7207200 | 720720 |
| 11 | 7207200 | 655200 |
| 12 | 7207200 | 600600 |
| 13 | 7207200 | 554400 |
| 14 | 7207200 | 514800 |
| 15 | 7207200 | 480480 |
| 16 | 7207200 | 450450 |

si on exécute donc notre code java avec ce jeu de test on obtien la courbe de scalabilité forte suivante : 

<img src=images_rapport\courbe_scalabilite_forte_assignment102.png alt="courbe de scalabilite forte d'assignment102.java" style="width:50%;"> <br><small><small>*Courbe de scalabilite forte d'assignment102.java*</small></small></img>

sur ce graphique on peut voir en rouge la courbe optimale de scalabilité forte et en bleu la courbe de scalabilité forte de `Assignment102.java`. On remarque que la courbe de `Assignment102.java` ne correspond pas du tout à la courbe optimale, ce qui veux dire que `Assignment102.java` à une mauvaise scalabilité forte, ce qui peut être dû à plusieur facteurs comme le `AtomicInteger` qui rend le code trop séquentiel et donc qui annule les effets positifs de la parallélisation.

**Scalabilité faible :**<br>
Comme plus tôt la scalabilité faible consiste en un problème dont la taille augmente proportionnellement au nombre de processus.

voici le tableau des valeur utilisé pour l'experience de scalabilité forte de `Assignment102.java`:

| Nombre de processus | taille du problème | taille du problème pour chaque processus |
| -- | -- | --|
| 1 | 1000000 | 1000000 |
| 2 | 2000000 | 1000000 |
| 3 | 3000000 | 1000000 |
| 4 | 4000000 | 1000000 |
| 5 | 5000000 | 1000000 |
| 6 | 6000000 | 1000000 |
| 7 | 7000000 | 1000000 |
| 8 | 8000000 | 1000000 |
| 9 | 9000000 | 1000000 |
| 10 | 10000000 | 1000000 |
| 11| 11000000 | 1000000 |
| 12 | 12000000 | 1000000 |
| 13 | 13000000 | 1000000 |
| 14 | 14000000 | 1000000 |
| 15 | 15000000 | 1000000 |
| 16 | 16000000 | 1000000 |

si on exécute donc notre code java avec ce jeu de test on obtien la courbe de scalabilité forte suivante : 

<img src=images_rapport\courbe_scalabilite_faible_assignment102.png alt="courbe de scalabilite faible d'assignment102.java" style="width:50%;"> <br><small><small>*Courbe de scalabilite faible d'assignment102.java*</small></small></img>

sur ce graphique on à en rouge la courbe optimale pour la scalabilité faible et en bleu la courbe de scalabilité faible de `Assignment102.java`.
Encore une fois on remarque que la coubre de `Assignment102.java` ne suit pas du tout la courbe optimale ce qui nous montre que `Assignment102.java` a une mauvaise scalabilié faible, on dirait même que la courbe descent de proportionnellement au nombre de coeur, donc aussi proportionnellement à la taille du problème, ce qui peut nous indiquer tout comme la courbe de scalabilité forte que l'ajout de processus n'a aucun impacte sur les performance du programme `Assignment102.java`.


### <a name="p4c"></a>  c) - Réalisation des tests de scalabilité sur `Pi.java` : 

#### <a name="p4c1"></a>1) - Modification du code `Pi.java` :

```java
public class Pi 
{
    public static void main(String[] args) throws Exception 
    {
        long total=0;
        total = new Master().doRun(50000, 10);
        System.out.println("total from Master = " + total);
    }
}
```

On peut voir ci-dessus l'ancienne de `Pi.java` qui n'était pas encore exécutable à l'aide d'une commande tout en modifiant ces parametre, alors que la version ci-dessous est une version modifier de la même façon que la version modifier de `Assignment102.java` ce qui lui permet donc d'être lancer avec le script python créer précédemment.

```java
public class Pi
{
    public static void main(String[] args) throws Exception
    {
        long total=0;
        long numIterations = Long.parseLong(args[0]);
        int numWorkers = Integer.parseInt(args[1]);
        total = new Master().doRun(numIterations, numWorkers,args[2]);
        System.out.println("total from Master = " + total);
    }
}
```

#### <a name="p4c2"></a> 3) Test de performance de `Pi.java` :

Pour réaliser les tests de performance de `Pi.java`, on va utiliser les même jeux de tests, donc si vous voulez les revoir je vous invite à revoir la partie [**Test de performance de `Assignment102.java`**](#p4b3).

**Scalabilité forte :**<br>

Si on exécute le code de `Pi.java` avec le jeu de tests utilisé pour la scalabilité forte on obtien cette courbe : 

<img src=images_rapport\courbe_scalabilite_forte_pi.png alt="courbe de scalabilite forte de Pi.java" style="width:50%;"> <br><small><small>*Courbe de scalabilite forte d'pi.java*</small></small></img>

sur ce graphique on peut voir en rouge la courbe optimale de scalabilité forte et en bleu la courbe de scalabilité forte de `Pi.java`.
Sur cette courbe on remarque que l'évolution de la scalabilité est constante jusqu'à 8 processus mais qu'au delà elle stagne voir décrois, cela peut s'expliquer par le nombre de de coeur physique du processeur qui est de 8 docn au delà il passe en hyperthreading ce qui ne permet pas une nette amélioration des performance.

<img src=images_rapport\courbe_scalabilite_faible_pi.png alt="courbe de scalabilite faible de Pi.java" style="width:50%;"> <br><small><small>*Courbe de scalabilite faible de pi.java*</small></small></img>

sur ce graphique on peut voir en rouge la courbe optimale de scalabilité faible et en bleu la courbe de scalabilité faible de `Pi.java`.
On remarque que la courbe décroit assez lentement, on passe de 1 avec un seul processus à 0.84 avec 16 ce qui est positif si on compare ces résultat avec ceux d' `Assignment102.java`, on peut donc en conclure que `Pi.java` a une bonne scalabilité faible.

---
## <a name="p5"></a> V - Mise en oeuvre mémoire distribuée :

Dans cette partie on va étudier une réalisation de l'agorithme de Monte-Carlo pour calculer Pi en mémoire distribuée avec le paradigme **Master Worker**, pour cela on va étudier deux code java : `MasterSocket.java`, qui va jouer le rôle de Master et `WorkerSocket.java`, qu va jouer le rôle de Worker.

Pour commencer on va réaliser un diagramme de classe UML pour comprendre les différentes relations entre les classes : <br>

<img src=images_rapport\diagramme_UML_Socket.png alt="diagramme de classe UML de `MasterSocket.java` et de `WorkerSocket.java`" style="width:50%;"> <br><small><small>*diagramme de classe UML de `MasterSocket.java` et de `WorkerSocket.java`*</small></small></img>

**Liste des relations**


| **Relation**                          | **Type**                | **Exemple dans le Code**                                           |
|---------------------------------------|-------------------------|---------------------------------------------------------------------|
| `MasterSocket → WorkerSocket`         | **Dépendance (via réseau)** | `MasterSocket` envoie des commandes à `WorkerSocket` à travers les `Socket`. |
| `MasterSocket → Socket`               | **Agrégation**          | `MasterSocket` gère un tableau de `Socket` pour communiquer avec les Workers (`sockets[]`). |
| `WorkerSocket → Socket`               | **Composition**         | `WorkerSocket` utilise un `Socket` pour établir et maintenir une connexion réseau. |
| `Socket → PrintWriter / BufferedReader` | **Dépendance**          | `Socket` fournit des flux (`getOutputStream`, `getInputStream`) nécessaires à `PrintWriter` et `BufferedReader`. |
| `MasterSocket → PrintWriter`          | **Association**         | `MasterSocket` utilise `PrintWriter` pour envoyer des données aux Workers (`writer[i]`). |
| `MasterSocket → BufferedReader`       | **Association**         | `MasterSocket` utilise `BufferedReader` pour lire les réponses des Workers (`reader[i]`). |
| `WorkerSocket → PrintWriter`          | **Association**         | `WorkerSocket` utilise `PrintWriter` pour envoyer des messages au Master (`pWrite`). |
| `WorkerSocket → BufferedReader`       | **Association**         | `WorkerSocket` utilise `BufferedReader` pour lire les commandes provenant du Master (`bRead`). |

Ce code utilise le paradigme **Master Worker** comme `Pi.java` dans la partie [3.b](#p3b). La seul différence est l'utisation d'une version en mémoire distribuée. Chacun des Worker est un serveur sur lequel va s'exécuter un problème de petite taille, ce serveur peut être sur la même machine que le Master ou bien sur une autre machine à laquelle le Master va pouvoir y connecter un worker via *SSH*.

---
## <a name="p6"></a> VI - Calcul de performance en mémoire distribuée :

Dans cette partie on va calculer la scalabilité forte et la scalabilité faible de l'algorithme de MonteCarlo en mémoire distribuée avec les codes `MasterSocket.java` et `WorkerSocket.java`.<br>
Pour réaliser ces tests de scalabilité on va utiliser les même tableau que pour les tests de scalabilité de `Assignment102.java` et `Pi.java`.<br>

On obtien donc les courbes suivantes :<br>

<img src=images_rapport\courbe_scalabilite_forte_memoir_distribuee.png alt="courbe scalabilité forte memoire distriuée" style="width:50%;"> <br><small><small>*courbe de scalabilité forte en memoire distriuée*</small></small></img>

<img src=images_rapport\courbe_scalabilite_faible_memoir_distribuee.png alt="courbe scalabilité faible memoire distriuée" style="width:50%;"> <br><small><small>*courbe de scalabilité faible en memoire distriuée*</small></small></img>

On remarque que ces deux courbe ressemble à celles de `Pi.java` ce qui est logique car les deux programme utilisent le paradigme **Master Worker**.

Si on veux pouvoir vraiment tester les performance de ce code en memoire distribuée il faut essayer de le lancer sur plusieures machine en même temps, cette experience a été réalisée en cours de Programmation Avancée en salle G24 :

Voici le tableau des tests réalisés pour cette experience : 

| Machine | forte | faible | Processus |
|---------|-------|--------|-----------|
| 1 | 192e6 | 16e6 | 4 |
| 2 | 192e6 | 32e6 | 8 |
| 3 | 192e6 | 48e6 | 12 |
| 4 | 192e6 | 64e6 | 16 |
| 5 | | | 20 |
| 6 | 192e6 | 96e6 | 24 |
| 7 | | | 28 |
| 8 | 192e6 | 128e6 | 32 |
| 9 | | | 36 |
| 10 | | | 40 |
| 11 | | | 44|
| 12 | 192e6 | 192e6 | 48 |

on va donc pouvoir tester notre code sur 12 et 48 processus.<br>
Un fois l'experience réalisée on obtien les courbes suivantes : 

<img src=images_rapport\scalabilite_forte_memoire_distribuee.png alt="courbe scalabilité forte memoire distriuée sur plusieures machines" style="width:50%;"> <br><small><small>*courbe de scalabilité forte en memoire distriuée sur plusieures machines*</small></small></img>

<img src=images_rapport\scalabilite_faible_memoire_distribuee.png alt="courbe scalabilité faible memoire distriuée sur plusieures machines" style="width:50%;"> <br><small><small>*courbe de scalabilité faible en memoire distriuée sur plusieures machines*</small></small></img>

on remarque que les deux courbes sont presque parfaites ce qui montre que le code en mémoire distribuée à une excellente scalabilité faible et forte

---
## <a name="p7"></a> VII - calcul d'erreur :

Dans cette partie on va étudier le bon fonctionnement du programme en termes de respet du résultat attendu, dans notre cas on va analyser l'erreur, c'est à dire la différence entre Pi et la valeur envoyer par nos différents programme java en fonction du nombre de points choisi aléatoirement.

Pour cela on va générer des nuages de points pour chacun des programme java et regarder si la moyenne de l'erreur diminue si on augmente le nombre de points, voici donc les résultats :

<img src=images_rapport\nuage_point_erreur_assignment102.png alt="nuage depoint d'erreur de Assignment102.java" style="width:50%;"> <br><small><small>*nuage depoint d'erreur de Assignment102.java*</small></small></img>


<img src=images_rapport\nuage_point_erreur_pi.png alt="nuage depoint d'erreur de Pi.java" style="width:50%;"> <br><small><small>*nuage depoint d'erreur de Pi.java*</small></small></img>


Les deux figures ci-dessus représentent des nuages de points avec en bleu, les points des tirages alléatoires et en rouge la moyenne en fonction du nombre de tirages.<br>
On remarque que pour ``Assignment102.java`` et ``Pi.java`` la moyenne de l'erreur diminue bien si on augmente le nombre de points, ce qui veux dire que les deux programme font bien ce pourquoi ils ont étés créer, c'est à dire calcule une valeur approchée de Pi.
