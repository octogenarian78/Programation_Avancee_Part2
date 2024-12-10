# Rapport Programmation Avancée partie 2


## Introduction
*écrir l'intro*


---

## Plan

- ### [I – Méthode de Monte-Carlo pour calculer Pi](#p1)
- ### [II - Algorithme et parallélisation](#p2)
  - #### [**a) - Itération parallèle**](#p2a)
  - #### [**b) - Master Worker**](#p2b)
- ### [III - Algorithme et parallélisation](#p3)
    - #### [**a) - Analyse de Assignment102.java**](#p3a)
  <br><br><br>

---

## <a name="p1"></a> I - Méthode de Monte-Carlo pour calculer Pi :

La méthode de Monte-Carlo est une de probabilité qui permet d'éstimer la valeu de Pi, elle fonctionne de la façon suivante : 

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

    xp = valeur alléatoire en 0 et 1
    yp = valeur alléatoire en 0 et 1

    si (xp au carré + yp au carré) inferieur à 1
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
    xp = valeur alléatoire en 0 et 1
    yp = valeur alléatoire en 0 et 1

    renvoyer booleen (xp au carré + yp au carré) inferieur à 1
fin fonction

initialiser n_cible à 0

pour p allant de 0 à n_tot-1

    si tirer_point()
        ajouter 1 à n_cible
    fin si

fin pour

calculer pi = 4 * n_cible / n_tot
```

`tirer_point()` peut être exécuter sur plusieurs *threads* en même tempscar chacune de ses exécutions sont indépendantes les unes des autres.

#### Identifiaction de ressouce critique :

Vu que les différents **T0p1** ne dépendent pas les uns des autres et que les différents **T0p2** ne dépendent pas non plus les uns des autres, ces deux sous-taches peuvent être parallélisées, c'est à dire être exécuter sur plusieurs *threads* en même temps, mais il faut faire attention à de possibles ressources critiques, des zones mémoires pour lesquelles plusieurs *threads* peuvent vouloir accéder en même temps.<br>

Dans notre cas la ressource critique serai ``n_cible`` que plusieurs *threads* pourrais vouloir modifier en même temps, il faut donc, pour cela, donc réstrindre l'accés à la section critique ``ajouter 1 à n_cible``

### <a name="p2b"></a> b) - Master Worker :

Le paradigme **Master Worker**, consiste en un **Master** qui va diviser une grosse tache qu'il va diviser en plus petites taches qui vont être réparties sur différents **Workers** qui vont ensuite la réaliser et renvoyer leur résultat au **Worker** qui va pouvoir les traiter et les analyser.

<img src=images_rapport\diagramme_master_worker.png alt="diagramme fonctionnement Maset Worker" style="width:50%;"> <br><small><small>*Diagramme reprsentant le fonctionnement du paradigme Master Worker*</small></small></img>

Ce paradigme peut être utiliser pour le calcul de Pi avec la méthode de Monte-Carlo avec par exemple le code suivant :

<br>

*pseudo code du Worker*<br>

```
Worker_MC : parametre(n_tot)
    initialiser n_cible à 0

    pour p allant de 0 à n_tot-1

        xp = valeur alléatoire en 0 et 1
        yp = valeur alléatoire en 0 et 1

        si (xp au carré + yp au carré) inferieur à 1
            ajouter 1 à n_cible
        fin si

    fin pour

revoyer n_cible
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

`Assignment102.java` utilise le parallelisme iteratif comme proposé dans la partie `II.a`.<br>
Le problème de `Assignment102.java` est la ressource critique `nAtomSuccess` qui est de type `AtomicInteger`, ce qui veux dire que la valeur ne peut être manipulée que par une tache à la fois ce qui fait que dans le cas actuel si on verifie que que le point choisi aléatoirement est bien dans le quart de disque alors 75% de l'exécution se fait en itératif, une améloiration envisageable serai de verifier si le point choisi aléatoirement n'est pas dans le quart de disque comme ça seulement 25% de l'exécution serai en iteratif

### <a name="p3b"></a> b) - Analyse de `Pi.java` :

Le programme `Pi.java` calcule Pi avec la méthode de Monte-Carlo en utilisant le paradigme **Master Worker**, il est composé de trois classes : `Pi`, `Master`et `Worker`.<br>
Afin de mieux se représenter ce programme on va réaliser un diagramme de classe en UML : 

<img src=images_rapport\diagramme_UML_Pi.png alt="diagramme du code Pi.java" style="width:50%;"> <br><small><small>*Diagramme de classe de Pi*</small></small></img>

Comme dit précédemment `Pi.java` utilise le paradigme de programmation **Master Worker**, comme proposé dans la partie `II.b`.<br>
Avec l'utilisation du paradigme de programmation **Master Worker** on évite le problème de la ressouce critique, car chaques taches possèdent sont propre compteur, donc aucune ressource critique.