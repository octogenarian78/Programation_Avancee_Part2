# Rapport Programmation Avancée partie 2


## Introduction
*écrir l'intro*


---

## Plan

- ### [I – Méthode de Monte-Carlo pour calculer Pi](#p1)
- ### [II - Algorithme et parallélisation](#p2)
  - #### [**a) - Itération parallèle**](#p2a)
  - #### [**b) - Master Worker**](#p2b)
    
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

    si (xp au carré + yp au carré) supérieur à 1
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

        si (xp au carré + yp au carré) supérieur à 1
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