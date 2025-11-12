package compilation.lisp;
import java.io.*;

/* Interpreteur Lisp */

public class Lisp {

final int type_echec = -1;

final String nomfich_SOURCE = "test_lisp.txt";

String chaine_atomique;

public class Ptr_liste {
   Ptr_liste ptr_arriere;
   boolean type_liste = false;
   
   Ptr_liste debut;
   Ptr_liste suite;
   String atome;
   
   public Ptr_liste() {
   }
}

File fichier_SOURCE;
DataInputStream dis;
RandomAccessFile raf;

int pos_fichier;

Ptr_liste pointeur, expr, l_ass, ptr_true, ptr_atome;
String ch;

boolean erreur;

int code,valeur;
String chaine;

/*  Gestion des Erreurs:  */

void type_erreur(int numero)
{
   erreur=true;
   switch (numero) {
      case 0: System.out.println("Erreur #0: Erreur Lexicale"); break;
      case 1: System.out.println("Erreur #1: Erreur Syntaxique"); break;
      case 2: System.out.println("Erreur #2: ?"); break;
   }
}

Ptr_liste trans(String chaine_atome)
/* chaine de caracteres --> Liste atomique */
{
   // ptr_atome = new Ptr_liste();	
   ptr_atome.atome = chaine_atome;
   ptr_atome.type_liste = false;
   
   Ptr_liste trans_result;
   trans_result = ptr_atome;
   return trans_result;
}

/* --------------- Primitives LISP ------------------ */

Ptr_liste atom(Ptr_liste l)
{
   Ptr_liste atom_result;
   if (!(l.type_liste))  atom_result = ptr_true;
   else			 atom_result = null;
   return atom_result;
}

String print(Ptr_liste l)
{
   // if ((l!=null) && (atom(l) == ptr_true))  System.out.println("l.atome =" + l.atome);
   	
   String print_result;
   if (l == null)             	  print_result = "null";
   else if (atom(l) == ptr_true)  print_result = l.atome;
   else {
      print_result = "("
      + print(l.debut)
      + " "
      + print(l.suite)
      + ")";
   }   
   return print_result;
}

Ptr_liste number(Ptr_liste l)
{
   int nb = 0, erreur ;

   Ptr_liste number_result = null;;

   try {
      nb = Integer.parseInt(l.atome);	
      if (atom(l) == ptr_true) number_result = ptr_true;
   }
   catch (Exception e) {
      // System.out.println(e);
   }
   
   return number_result;
}

Ptr_liste isnull(Ptr_liste l)
{
   Ptr_liste null_result;
   if ( (l == null) || ((l.type_liste == false) && (l.atome=="NIL")) ) null_result = ptr_true;
   else null_result = null;
   return null_result;
}

Ptr_liste eq(Ptr_liste x,Ptr_liste y)
{
   Ptr_liste eq_result;
   if ((x == null) && (y == null)) eq_result = ptr_true;
   else if (x == null) eq_result = null;
   else if (y == null) eq_result = null;
   else if ( (x.type_liste == false) &&
        (y.type_liste == false) &&
        (x.atome.equals(y.atome)) )  eq_result = ptr_true;
   else eq_result = null;
   return eq_result;
}

Ptr_liste car(Ptr_liste l)
{
   Ptr_liste car_result;
   if (isnull(l) == ptr_true)  	  car_result = null;
   else if (atom(l) == ptr_true)  car_result = l;
   else car_result = l.debut;
   return car_result;
}

Ptr_liste cdr(Ptr_liste l)
{
   Ptr_liste cdr_result;
   if (isnull(l) == ptr_true)  	 cdr_result = null;
   else if (atom(l) == ptr_true) cdr_result = l;
   else cdr_result = l.suite;
   return cdr_result;
}

Ptr_liste caar(Ptr_liste l)
{
   Ptr_liste caar_result;
   caar_result = car( car(l) );
   return caar_result;
}

Ptr_liste cadr(Ptr_liste l)
{
   Ptr_liste cadr_result;
   cadr_result = car( cdr(l) );
   return cadr_result;
}

Ptr_liste cdar(Ptr_liste l)
{
   Ptr_liste cdar_result;
   cdar_result = cdr( car(l) );
   return cdar_result;
}

Ptr_liste cddr(Ptr_liste l)
{
   Ptr_liste cddr_result;
   cddr_result = cdr( cdr(l) );
   return cddr_result;
}

Ptr_liste caddr(Ptr_liste l)
{
   Ptr_liste caddr_result;
   caddr_result = car( cdr( cdr(l) ));
   return caddr_result;
}

Ptr_liste cadar(Ptr_liste l)
{
   Ptr_liste cadar_result;
   cadar_result = car( cdr( car(l) ));
   return cadar_result;
}

Ptr_liste caddar(Ptr_liste l)
{
   Ptr_liste caddar_result;
   caddar_result = car( cdr( cdr( car(l) )));
   return caddar_result;
}

Ptr_liste cons(Ptr_liste x, Ptr_liste y)
/*
a = (1 2) , b = (3 4)
(cons a b) = ((1 2) 3 4)
*/
{
   Ptr_liste ptr;

   Ptr_liste cons_result;
   ptr = new Ptr_liste();
   ptr.type_liste = true;

   if ((y == null) && (x.type_liste == true))  cons_result = x;
   else
   {
      ptr.debut = x;
      ptr.suite = y;
      cons_result = ptr;
   }
   return cons_result;
}

Ptr_liste concat(Ptr_liste u, Ptr_liste v)
/*
Concatene 2 listes:
a = (1 2) , b = (3 4)
(concat a b) = (1 2 3 4)
*/
{
   Ptr_liste concat_result;
   if (isnull(u) == ptr_true)  concat_result = v;
   else concat_result = cons( car(u), concat( cdr(u),v ) );
   return concat_result;
}

Ptr_liste list(Ptr_liste x, Ptr_liste y)
/*
a = (1 2) , b = (3 4)
(list a b) = ((1 2) (3 4))
*/
{
   Ptr_liste ptr;

   Ptr_liste list_result;
   ptr = new Ptr_liste();
   ptr.type_liste = true;
   ptr.debut = x;
   ptr.suite = y;
   list_result = ptr;
   return list_result;
}

Ptr_liste add(Ptr_liste x, Ptr_liste y)
{
   Ptr_liste ptr;
   int op1=0,op2=0,erreur;

   Ptr_liste add_result;
   ptr = new Ptr_liste();
   ptr.type_liste = false;

   try {
      op1 = Integer.parseInt(x.atome);	
      op2 = Integer.parseInt(y.atome);	
   }
   catch (Exception e) {
      System.out.println(e);
   }
   ptr.atome = String.valueOf(op1+op2);
   add_result = ptr;
   return add_result;
}

Ptr_liste sub(Ptr_liste x, Ptr_liste y)
{
   Ptr_liste ptr;
   int op1=0,op2=0,erreur;

   Ptr_liste sub_result;
   ptr = new Ptr_liste();
   ptr.type_liste = false;

   try {
      op1 = Integer.parseInt(x.atome);	
      op2 = Integer.parseInt(y.atome);	
   }
   catch (Exception e) {
      System.out.println(e);
   }
   ptr.atome = String.valueOf(op1-op2);
   sub_result = ptr;
   return sub_result;
}

Ptr_liste egal(Ptr_liste x, Ptr_liste y)
{
   Ptr_liste ptr;
   int op1=0,op2=0,erreur;

   Ptr_liste egal_result;
   try {
      op1 = Integer.parseInt(x.atome);	
      op2 = Integer.parseInt(y.atome);	
   }
   catch (Exception e) {
      System.out.println(e);
   } 
   if (op1 == op2)  egal_result = ptr_true;
   else egal_result = null;
   return egal_result;
}

Ptr_liste mul(Ptr_liste x, Ptr_liste y)
{
   Ptr_liste ptr;
   int op1=0,op2=0,erreur;

   Ptr_liste mul_result;
   ptr = new Ptr_liste();
   ptr.type_liste = false;

   try {
      op1 = Integer.parseInt(x.atome);	
      op2 = Integer.parseInt(y.atome);	
   }
   catch (Exception e) {
      System.out.println(e);
   } 
   ptr.atome = String.valueOf(op1*op2);
   mul_result = ptr;
   return mul_result;
}

Ptr_liste sexpr(Ptr_liste u, Ptr_liste ass)
/*
�valuation d'une liste de sexpressions
*/
{
   Ptr_liste sexpr_result;
   if (isnull(u) == ptr_true)  sexpr_result = null;
   else sexpr_result = cons( eval( car(u),ass ) , sexpr( cdr(u),ass ) );
   return sexpr_result;
}

Ptr_liste pair(Ptr_liste u, Ptr_liste v)
/*
prend 2 listes et associe
les elements correspondants
*/
{
   Ptr_liste pair_result;
   if (isnull(u) == ptr_true)  pair_result = null;
   else pair_result = list(
               cons( car(u), car(v) ) ,
               pair( cdr(u),cdr(v) ));
   return pair_result;
}

Ptr_liste cond(Ptr_liste u, Ptr_liste ass)
/*
�valuation de la conditionnelle
*/
{
   Ptr_liste cond_result;
   if (eval( caar(u),ass ) == ptr_true) 
   cond_result = eval( cadar(u),ass );
   else cond_result = cond( cdr(u),ass );
   return cond_result;
}

Ptr_liste assoc(Ptr_liste expr, Ptr_liste ass)
/*
Lecture dans la liste associative:
expr: nom de la fonction en entree
ASSOC: corps de la fonction en sortie
*/
{
   Ptr_liste assoc_result;
   if (isnull(ass) == ptr_true)  assoc_result = null;
   else if (eq( expr,caar(ass) ) == ptr_true)  assoc_result = car(ass);
   else assoc_result = assoc( expr,cdr(ass) );
   return assoc_result;
}

Ptr_liste modif(Ptr_liste expr, Ptr_liste val, Ptr_liste ass)
/*
Ecriture dans la liste associative:
( modification d'un couple deja existant )
*/
{
   Ptr_liste modif_result;
   if (isnull(ass) == ptr_true)  modif_result = null;
   else if (eq( expr,caar(ass) ) == ptr_true) 
        modif_result = cons( list(expr,val), modif( expr,val,cdr(ass) ));
   else modif_result = cons( car(ass), modif( expr,val,cdr(ass) ));
   return modif_result;
}

Ptr_liste ajout(Ptr_liste expr, Ptr_liste val, Ptr_liste ass)
/*
Ecriture dans la liste associative:
*/
{
   Ptr_liste ajout_result;
   if (isnull( assoc(expr,ass) ) == ptr_true) 
      ajout_result = concat( l_ass, list( list(expr,val),null ));
   else ajout_result = modif( expr,val,ass );
   return ajout_result;
}

Ptr_liste eval(Ptr_liste expr, Ptr_liste ass)
{
   Ptr_liste eval_result;
   ptr_atome = new Ptr_liste();
   ptr_atome.type_liste = false;

        if (atom(expr) == ptr_true) 
        {
           // System.out.println("atom");	
           /* c'est un atome */
                if (eq( expr , null ) == ptr_true)            eval_result = null;
           else if (eq( expr , trans("T") ) == ptr_true)      eval_result = ptr_true;
           else if (eq( expr , trans("L_ASS") ) == ptr_true)  eval_result = l_ass;
           else if (number(expr) == ptr_true)                 eval_result = expr;
           else {
           	eval_result = cdr( assoc( expr , ass ) );
           }	
        }
   else if (atom( car(expr) ) == ptr_true) 
        /* c'est une liste de type (fonction arg1 arg2 ...) */
        {
           // System.out.println("liste");
           Ptr_liste res = null;
                               
           /* fonctions a 1 parametre: */
                if (eq( car(expr) , trans("CAR") ) == ptr_true) 
                     eval_result = car( eval( cadr(expr) , ass ) );
           else if (eq( car(expr) , trans("CDR") ) == ptr_true) 
                   eval_result = cdr( eval( cadr(expr) , ass ) );
           else if (eq( car(expr) , trans("ATOM") ) == ptr_true) 
                   eval_result = atom( eval( cadr(expr) , ass ) );
           else if (eq( car(expr) , trans("NULL") ) == ptr_true) 
                   eval_result = isnull( eval( cadr(expr) , ass ) );
           /* fonctions � 2 param�tres: */
           else if (eq( car(expr) , trans("CONS") ) == ptr_true) 
                {
                   eval_result = cons( eval( cadr(expr)  , ass ) ,
                               eval( caddr(expr) , ass ) );
                }
           else if (eq( car(expr) , trans("EQ") ) == ptr_true) 
                   eval_result = eq( eval( cadr(expr)  , ass ) ,
                             eval( caddr(expr) , ass ) );
           else if (eq( car(expr) , trans("+") ) == ptr_true) 
                   eval_result = add( eval( cadr(expr)  , ass ) ,
                              eval( caddr(expr) , ass ) );
           else if (eq( car(expr) , trans("-") ) == ptr_true) 
                   eval_result = sub( eval( cadr(expr)  , ass ) ,
                              eval( caddr(expr) , ass ) );
           else if (eq( car(expr) , trans("=") ) == ptr_true) 
                   eval_result = egal( eval( cadr(expr)  , ass ) ,
                              eval( caddr(expr) , ass ) );
           else if (eq( car(expr) , trans("*") ) == ptr_true) 
                   eval_result = mul( eval( cadr(expr)  , ass ) ,
                              eval( caddr(expr) , ass ) );
           else if (eq( car(expr) , trans("DEFINE") ) == ptr_true) 
                {
                   // System.out.println("define");
                   l_ass = ajout( cadr(expr) , caddr(expr) , l_ass);
                   eval_result = l_ass;
                }
           /* fonctions speciales quote & cond */
           else if (eq( car(expr) , trans("QUOTE") ) == ptr_true) 
                   eval_result = cadr(expr);
           else if (eq( car(expr) , trans("COND") ) == ptr_true) 
                   eval_result = cond( cdr(expr) , ass );

          /* ce n'est pas une primitive de lisp */
          // else if (eq( car(expr) , null ) == ptr_true)  eval_result = null;
          else {
          	// System.out.println("not a lisp primitive");
                eval_result = eval(cons(cdr(assoc(car(expr),ass)), cdr(expr)), ass);
          }
        }
   else if (eq( caar(expr) , trans("LAMBDA") ) == ptr_true) 
   {
      // System.out.println("lambda");	
          
      if (ass == null) 
           eval_result = eval( caddar(expr),
           cons( pair( cadar(expr),sexpr(cdr(expr),ass)),ass));
      else eval_result = eval( caddar(expr),
           concat( pair( cadar(expr),sexpr(cdr(expr),ass)),ass));

   }
   else eval_result = null;
   return eval_result;
}

/* ---------------------- Analyse Lexicale -------------------------- */

boolean lettre(char car)
/* lettre -> A..Z | a..z | _ */
{
   boolean lettre_result;

   if ( ((car>='A') && (car<='Z'))
	|| ((car>='a') && (car<='z'))
	|| (car == '_') ) lettre_result=true;
   else lettre_result=false;

   return lettre_result;
}

boolean operator(char car)
/* operator -> =, * , +, - */
{
   boolean operator_result;
   switch (car) {
      case '=': case '*': case '+': case '-': operator_result=true;
      break;

      default: operator_result = false;
   }
   return operator_result;
}

boolean chiffre(char car)
/* chiffre -> Hexadecimal */
{
   boolean chiffre_result;
   switch (car) {
      case '0': case '1': case '2': case '3': case '4' : case '5':
      case '6': case '7': case '8': case '9': chiffre_result=true;
      break;

      default: chiffre_result = false;
   }
   return chiffre_result;
}

// boolean blanc(int pos_fichier) throws IOException
boolean blanc() throws IOException 
/* blanc -> #32 | #10 | #13 */
{
   char car;
   boolean blanc_result;

   try {
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();

      switch (car) {
         case '\40':
         case '\12':
         case '\15': {pos_fichier=pos_fichier+1; blanc_result=true;} break;

         default: blanc_result=false;
      }
   }
   catch (IOException ioe) {
      throw ioe;
   }

   return blanc_result;
}

boolean ouvrante() throws IOException 
/* Ouvrante ->  (  */
{
   char car;
   boolean ouvrante_result;

   try {
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();

      switch (car) {
         case '('  : {
                pos_fichier = pos_fichier + 1;
                ouvrante_result = true;
             }
             break;
      default:   ouvrante_result = false;
      }
   }
   catch (IOException ioe) {
      throw ioe;
   }

   return ouvrante_result;
}

boolean fermante() throws IOException 
/* Fermante ->  )  */
{
   char car;
   boolean fermante_result;

   try {
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();

      switch (car) {
         case ')'  : {
                pos_fichier = pos_fichier + 1;
                fermante_result = true;
             }
             break;
      default:   fermante_result = false;
      }
   }
   catch (IOException ioe) {
      throw ioe;
   }

   return fermante_result;
}

// boolean id2(int pos_fichier, String chaine) throws IOException
boolean id2() throws IOException
/* id2 -> lettre id2 | chiffre id2 | vide */
{
   char car;
   boolean id2_result;

   try {
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();

      if (lettre(car) || chiffre(car) || operator(car))
      {
         pos_fichier = pos_fichier + 1;
         chaine = chaine + Character.toUpperCase(car);

         if (chaine.length() <= 16)  id2_result = id2(); // id2(pos_fichier,chaine);
         else id2_result = false;
      }
      else id2_result = true;
   }
   catch (IOException ioe) {
      throw ioe;
   }

   return id2_result;
}

// boolean id1(int pos_fichier, String chaine) throws IOException
boolean id1() throws IOException
/* id1 -> lettre id1 */
{
   char car;
   boolean id1_result;
   chaine="";

   // System.out.println("id1 : pos_fichier = " + pos_fichier);
   try {
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();
            
      if (lettre(car) || chiffre(car) || operator(car))
      {
         pos_fichier = pos_fichier + 1;
         chaine = chaine + Character.toUpperCase(car);
         id1_result = id2(); // id2(pos_fichier,chaine);
      }
      else id1_result = false;
   }
   catch (IOException ioe) {
      throw ioe;
   }

   // if (id1_result == true) System.out.println("chaine = " + chaine); 
   return id1_result;
}

// boolean commentaire(int pos_fichier) throws IOException
boolean commentaire() throws IOException
/* 
   commentaire -> { car } 
                 #123 car #125
   = parenthese
 */
{
   char car;
   int sortie;
   boolean parenthese_result;

   try { 
      raf.seek(pos_fichier);
      car = (char) raf.readUnsignedByte();

      if (car == '{')
      {
         sortie=0;
         do {
            pos_fichier = pos_fichier + 1;
            raf.seek(pos_fichier);
            car = (char) raf.readUnsignedByte();

            if (car == '}')  sortie = 1;
         } while (!(sortie != 0));

         pos_fichier = pos_fichier + 1;
         if (sortie == 1)  parenthese_result = true;
         else parenthese_result = false;
      }
      else parenthese_result = false;
   }
   catch (IOException ioe) {
      sortie = 2;
      throw ioe;
   }

   return parenthese_result;
}


// void lex(int pos_fichier, int code, int valeur, String chaine)
void lex()
{
   try {
      // System.out.println("pos_fichier = " + pos_fichier);
      while (blanc() || commentaire());
      // System.out.println("pos_fichier = " + pos_fichier);
            
      if (id1())		code = Loader.code_pos("ID");
      else if (ouvrante())	code = Loader.code_pos("(");
      else if (fermante())	code = Loader.code_pos(")");
      else 			code = type_echec;
      // System.out.println("code = " + code + " " + Loader.tab_terminaux[code]);
   }
   catch (IOException ioe) {
      code = Loader.code_pos("Fin");
   }
}

void affiche_lex()
{
   do {
      // lex(pos_fichier,code,valeur,chaine);
      lex();

      System.out.print(Loader.tab_terminaux[code]);

      if (code==Loader.code_pos("ID"))  System.out.print(' ' + chaine);
      
      System.out.println();
   } while (!( (code==Loader.code_pos("Fin")) || (code == type_echec)));

   if (code == type_echec)  type_erreur(0);
}

/*------------------------------------ ---------------------*/

void prod_1()
{
   expr = new Ptr_liste();
   pointeur = expr;
   pointeur.ptr_arriere = null;
}

void prod_2()
{
   pointeur.type_liste = true;
   pointeur.debut = new Ptr_liste();
   pointeur.suite = new Ptr_liste();
}

void prod_3()
{
   pointeur.type_liste = false;
   pointeur.atome = chaine;
}

void prod_4()
{
   Ptr_liste tampon;

   tampon = pointeur;
   pointeur = pointeur.debut;
   pointeur.ptr_arriere = tampon;
}

void prod_5()
{
   Ptr_liste tampon;

   tampon = pointeur;
   pointeur = pointeur.suite;
   pointeur.ptr_arriere = tampon;
}

void prod_6()
{
   pointeur.suite = null;
}

void prod_7()
{
   pointeur = pointeur.ptr_arriere;
}

void prod_8()
{
   System.out.println("? " + print(expr));
   System.out.println();
   System.out.println("> " + print( eval(expr,l_ass) ));
   System.out.println();
}

// -------------------------------------------------------------------------------

void produire_code()
{
   Liste_prod pointeur;

   pointeur = Loader.pile_analyse.ptr_lex;
   while (! (pointeur == null))
   {
      switch (pointeur.prod_lex) {
        case 1: prod_1(); break;
        case 2: prod_2(); break;
        case 3: prod_3(); break;
        case 4: prod_4(); break;
        case 5: prod_5(); break;
        case 6: prod_6(); break;
        case 7: prod_7(); break;
        case 8: prod_8(); break;
      }

      pointeur=pointeur.suivant;
   }
}

/*
----------------------- Analyse Syntaxique --------------------------
Algorithme de l'analyse :
Soit X le symbole en sommet de pile
et a le symbole d'entree courant

1: Si X=a DEPILER X
2: Si X est un non-terminal, consulter Table[X,a]
   c'est une Erreur --> Echec
   sinon DEPILER X , EMPILER Table[X,a]
   Ex: Table[X,a]=( X --> U V W )
   on DEPILE X , on EMPILE dans l'ordre W V U
   de maniere a avoir U en haut de la pile.
   (le travaille est facilite avec une table
   d'analyse inversee...)

On avance pos_fichier sur le symbole suivant,
et on recommence.
----------------------------------------------------------------------
*/

void analyse()
{
   Liste pointeur;

   Loader.pile_analyse = null;
   Loader.empiler_analyse(Loader.type_terminal,	Loader.code_pos("Fin"),	null);
   Loader.empiler_analyse(Loader.type_regle,	Loader.axiome,		null);
   
   // lex(pos_fichier,code,valeur,chaine);
   lex();
             
   if (code == type_echec)  type_erreur(0);

   if (Loader.pile_analyse == null) System.out.println("pile_analyse is null");   
   
   while (!( ((Loader.pile_analyse.code_lex == Loader.code_pos("Fin"))
               && (code == Loader.code_pos("Fin"))) || erreur))
   {
      /* X est un terminal (Code) */
           
      if (Loader.pile_analyse.type_lex == Loader.type_terminal)
         if (Loader.pile_analyse.code_lex == code)
         {
            // System.out.println("X est un terminal");	
            produire_code();

            /* Regle1 */
            Loader.depiler_analyse();

            // lex(pos_fichier,code,valeur,chaine);
	    lex();

            if (code == type_echec)  type_erreur(0);
         }
         else type_erreur(1);
      else

      /* X est un non-terminal (Regle) */

      {
         // System.out.println("X est un non-terminal (regle) "); 
         // System.out.println("Tab[ " + Loader.tab_regles[Loader.pile_analyse.code_lex] + " , " + Loader.tab_terminaux[code] + " ]");
                
         pointeur = Loader.tab_analyse[Loader.pile_analyse.code_lex][code];
         if (pointeur == null) System.out.println("pointeur null");
         
         if (! (pointeur == null))
         {
            produire_code();

            /* Regle2 */
            Loader.depiler_analyse();
            while (! (pointeur == null))
            {
               Loader.empiler_analyse(pointeur.type_lex,
                               pointeur.code_lex,
                               pointeur.ptr_lex);
               pointeur=pointeur.suivant;
            }
         }
         else type_erreur(1);
      }
   }
}

public Lisp() 
{
   try {
      fichier_SOURCE = new File(nomfich_SOURCE);
      raf = new RandomAccessFile(fichier_SOURCE,"r");
         
      erreur = false;
      
      ptr_true = new Ptr_liste();
      ptr_true.type_liste = false;
      ptr_true.atome = "T";
                  
      pointeur = null;
      expr = null;
      l_ass = null;
      ptr_atome = null;
   
      System.out.println("Interpreteur Lisp >");
      System.out.println("Code Source: " + nomfich_SOURCE);
      System.out.println();

      Loader.charge_table();
  
      pos_fichier = 0;
      analyse();
      prod_8();   
         
      if (!(erreur))  System.out.println("Succes");
      else	      System.out.println("Erreur");

      raf.close();
   }
   catch (Exception e) {
      System.out.println("Exception " + e);
   }
     
}

static public void main(String[] args) {
   Lisp lisp = new Lisp();
}

}