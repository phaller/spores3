package spores

import scala.quoted.*


private[spores] object Macros {

  /** Welcome to the internals of the Spores library! I salute your curiosity.
    * How do Spores work? Let's dive deep into closures/functions and the Spore
    * factory transformation.
    *
    * What is a closure? A "closure [is comprised of a] lambda-expression 
    * [(anonymous function)] and the environment relative to which it was
    * evaluated." (P. J. Landin: The Mechanical Evaluation of Expressions.
    * Comput. J. 6(4): 308-320 (1964))
    *   A lambda-expression may contain free identifiers (variables). An
    * environment associates these identifiers to values. The evaluation of a
    * lambda-expression "captures" the values of the identifiers from the
    * environment. This is illustrated by the following Scala 3 example.
    *   {{{
    * val y = 12
    * val f = (x: Int) => x + y 
    *   }}}
    *   The environment associates `y` to the value `12`. The lambda-expression
    * contains `y` as a free identifier. When the lambda-expression is evaluated
    * and assigned to `f`, the value `12` of the free identifier `y` is
    * captured. Evaluating `f(13)` yields `25`.
    *
    * It is problematic to seralize an arbitrary closure because its free
    * variables may not be serializable. Moreover, the programmer has no means
    * to access the free variables and their types. And so, it is difficult to
    * detect if a closure is serializable.
    *   The Spores library addresses this problem by transforming a closure into a
    * representation with an explicit (accessible) body and its captured
    * variables.
    *   In precise terms, the Spore factory transforms a closure with free
    * variables into a closure with no free variables by lifting all free
    * variables into function parameters.
    *
    * The factory method takes as arguments the closure `body` and the
    * sequence of captured variables `captures`. It performs the following
    * sequence of checks and transformations:
    *   We'll use the following shorthand notation . Let `b` denote the body or
    * closure, and `\seq(c_id)` = fv(b)` the sequence of free variables in `b`
    * where `c_id` corresponds to an identifier of a captured variable. The
    * runtime value of a closure is created by substituting its free variables
    * with their corresponding runtime values: `b[c_id -> c_val]`.
    *
    * (1) Well-formedness check on `captures`. The provided `captures` is in one
    * of three valid forms:
    * - `*`: capture all by default
    * - `\empty`: no capture
    * - `(c_id1, c_id2, ...)`: explicit capture list
    *
    * (2) Find captured identifiers in `body`.
    * - `bodyCaptures = fv(body)`
    *
    * It should be noted that identifiers that reference static (top-level)
    * fields or methods are not considered captured identifiers.
    *
    * (3) Check that `captures` is consistent with `bodyCaptures`.
    * - If `captures` is `*` then OK
    * - If `captures` is `()` then if `bodyCaptures` is empty then OK else ERROR
    * - If `captures` is `(c_id1, c_id2, ...)` and `bodyCaptures` is `(bc_id1,
    *    ...)`: then if for each `c_id` in `captures`: if `c_id` is in
    *    `bodyCaptures`: and if for each `bc_id` in `bodyCaptures`: if `bc_id`
    *    is in `captures`: then OK else ERROR.
    *
    * (4) Lift `bodyCaptures` into parameters:
    * - Transforms a `b` of shape `{ b }` with `(c_id1, c_id2, ...) =
    *   bodyCaptures = fv(b)` into `bNew`:
    *   `bNew = { (c_id1, c_id2, ...) => { b } }`
    *
    * (5) Create Spore by storing the transformed body `bNew` together with the
    * runtime values of the captured variables:
    * - `Spore0 = { body = bNew; captures = (c_val1, c_val2, ...) }`
    *
    * (6) A runtime value of the Spore's closure is created by application of
    * the Spore's `body` to the Spore's `captures`.
    */


  private[spores] def capturesWellFormednessCheck(capturesExpr: Expr[Seq[Any]])(using Quotes): (Int, List[quotes.reflect.Tree]) = {
    import quotes.reflect.*

    def expandInlined(term: Term): Term = term match {
      case Inlined(_, _, expansion) => expandInlined(expansion)
      case _ => term
    }

    capturesExpr match {
      case Varargs(Seq(star)) if star.asTerm.tpe <:< TypeRepr.of[Spore0.CaptureAllMode] => {
        (1, List())
      }
      case Varargs(captures) if captures.isEmpty => {
        (2, captures.map(_.asTerm).toList)
      }
      case Varargs(captures) => {
        val expanded = captures.map(c => expandInlined(c.asTerm)).toList
        if (expanded.exists(_.tpe <:< TypeRepr.of[Spore0.CaptureAllMode])) {
          report.error("Invalid capture list.")
          (-1, List())
        } else {
          (3, expanded)
        }
      }
      case _ => {
        report.error("Invalid capture list.")
        (-1, List())
      }
    }
  }


  private[spores] def findCapturedIds(using Quotes)(tree: quotes.reflect.Term): List[quotes.reflect.Tree] = {
    import quotes.reflect.*

    def findMaybeOwners(tree: Tree): List[Symbol] = {
      // Collect all symbols which are maybeOwners of other symbols in the tree.
      // This includes:
      // - ValDef
      // - DefDef
      // - ClassDef
      // - TypeDef

      val acc = new TreeAccumulator[List[Symbol]] {
        def foldTree(owners: List[Symbol], tree: Tree)(owner: Symbol): List[Symbol] = tree match {
          case x @ ValDef(_, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ DefDef(_, _, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ ClassDef(_, _, _, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ TypeDef(_, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case _ =>
            foldOverTree(owners, tree)(owner)
        }
      }
      acc.foldTree(List(), tree)(tree.symbol)
    }

    def findCaptures(anonfunBody: Tree): List[Tree] = {

      def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
        if (sym.maybeOwner.isNoSymbol) false else ((sym.maybeOwner == transitiveOwner) || ownerChainContains(sym.maybeOwner, transitiveOwner))

      def symIsToplevelObject(sym: Symbol): Boolean =
        sym.isNoSymbol || ((sym.flags.is(Flags.Module) || sym.flags.is(Flags.Package)) && symIsToplevelObject(sym.maybeOwner))

      def isOwnedByToplevelObject(sym: Symbol): Boolean =
        symIsToplevelObject(sym) || (!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.maybeOwner)

      // A symbol is owned by the spore if:
      // - it is owned by one of the maybeOwners of the spore
      // - it is a one of the maybeOwners of the spore

      val maybeOwners = findMaybeOwners(anonfunBody)
      def isOwnedBySpore(sym: Symbol): Boolean =
        maybeOwners.exists(ownerChainContains(sym, _)) || maybeOwners.contains(sym)

      def isThis(tree: Tree): Boolean = {
        tree match {
          case This(_) => true
          case _ => false
        }
      }

      // Collect all identifiers which *could* be captured by the closure. This
      // will also collect identifiers which are not captured by the closure
      // such as identifiers owned by toplevel objects, etc. These are later
      // filtered out.
      // This includes:
      // - Ident
      // - This
      // - TypeIdent

      def outermostAppliedTypeIdent(tree: Tree): Option[Tree] = {
        tree match {
          case x @ TypeIdent(_) => Some(x)
          case Applied(x @ _, _) => outermostAppliedTypeIdent(x)
          case _ => None
        }
      }

      val acc = new TreeAccumulator[List[Tree]] {
        def foldTree(ids: List[Tree], tree: Tree)(owner: Symbol): List[Tree] = tree match {
          case x @ Ident(_) if !x.symbol.isType =>
            x :: ids
          case x @ This(_) =>
            x :: ids
          // `TypeIdent`s are tricky... we want to find any occurrence of a
          // TypeIdent `T` in either of these cases: 
          // - `new T`
          // - `new T[U]`
          // - `class C extends T with ...`
          // - `class C extends T[U] with ...`
          // - `class C extends ... with T`
          // - `class C extends ... with T[U]`
          case ClassDef(_, _, parents, _, _) =>
            // `parents`` are either `TypeTree`s or `Term` containing `New`.
            // Here we collect all `TypeIdent`s from `TypeTree`s. 
            // The `folderOverTree` call collects the `TypeIdent`s from `New`.
            parents.flatMap { outermostAppliedTypeIdent(_) }
            ::: foldOverTree(ids, tree)(owner)
          case New(x) =>
            outermostAppliedTypeIdent(x).toList
            ::: foldOverTree(ids, tree)(owner)
          case _ =>
            foldOverTree(ids, tree)(owner)
        }
      }

      val foundIds = acc.foldTree(List(), anonfunBody)(anonfunBody.symbol.maybeOwner)

      // Filter out identifiers which are either:
      // - owned by a toplevel symbol or are a toplevel symbol
      // - owned by the spore
      foundIds
        .filter(tree => isThis(tree) || !isOwnedByToplevelObject(tree.symbol))
        .filter(tree => !isOwnedBySpore(tree.symbol))
    }

    findCaptures(tree)
  }


  private[spores] def checkCaptures(using Quotes)(captures: List[quotes.reflect.Tree], bodyCaptures: List[quotes.reflect.Tree]): Unit = {
    import quotes.reflect.*

    val capturesFullNameSet = captures.map(_.symbol.fullName).toSet
    val bodyCapturesFullNameSet = bodyCaptures.map(_.symbol.fullName).toSet

    captures.foreach {
      capture => {
        if (!bodyCapturesFullNameSet.contains(capture.symbol.fullName)) {
          report.error(s"`${capture.symbol.name}` is not captured by the spore body. Remove it from the capture list. It is a top-level variable or not used in the body.", capture.pos)
        }
      }
    }

    bodyCaptures.foreach {
      bodyCapture => {
        if (!capturesFullNameSet.contains(bodyCapture.symbol.fullName)) {
          bodyCapture match {
            case This(Some(qual)) => {
              report.error(s"Invalid capture of `this` from class ${qual}. Add it to the capture list or use `*` to capture all by default.", bodyCapture.pos)
            }
            case This(None) => {
              report.error(s"Invalid capture of `this` from outer class. Add it to the capture list or use `*` to capture all by default.", bodyCapture.pos)
            }
            case _ => {
              report.error(s"Invalid capture of variable `${bodyCapture.symbol.name}`. Add it to the capture list or use `*` to capture all by default.", bodyCapture.pos)
            }
          }
        }
      }
    }

    ()
  }


  private[spores] def findEvidence[F[_]](using Quotes)(captures: List[quotes.reflect.Tree])(using Type[F]): List[quotes.reflect.Tree] = {
    import quotes.reflect.*

    captures.flatMap { captured =>
      val capturedTpe = captured match {
        case x@ This(_) => captured.symbol.typeRef
        case _       => captured.symbol.termRef.widen
      }
      val evidenceTpe = TypeRepr.of[[T] =>> Spore0[F, F[T]]].appliedTo(capturedTpe)

      Implicits.search(evidenceTpe) match {
        case succ: ImplicitSearchSuccess => {
          List(succ.tree)
        }

        case fail: ImplicitSearchFailure => {
          // Do not use `evidenceTpe.show` here as it throws a MatchError on Scala 3.4.3 for some cases
          val msg = s"Missing implicit for captured variable `${captured.symbol.name}`.\n\n" + fail.explanation
          report.error(msg, captured.pos)
          List()
        }
      }
    }
  }


  private[spores] def liftBody(using Quotes)(owner: quotes.reflect.Symbol, bodyCaptures: List[quotes.reflect.Tree], body: quotes.reflect.Term): quotes.reflect.Term = {
    import quotes.reflect.*

    def substituteSymbols(subst: Map[Symbol, Term], body: Term): Term = {
      val treeMap = new TreeMap {
        override def transformTerm(t: Term)(o: Symbol): Term = {
          t match
            case x@ Ident(_) =>
              subst.getOrElse(x.symbol, super.transformTerm(t)(o))
            case x@ This(_) =>
              subst.getOrElse(x.symbol, super.transformTerm(t)(o))
            case _ =>
              super.transformTerm(t)(o)
        }
      }
      treeMap.transformTerm(body)(owner)
    }

    def liftSymbolGroup(symGroup: List[Tree], body: Term): Term = {
      val captureType = symGroup.head match {
        case x@ This(_)  => x.symbol.typeRef.asType
        case x@ _        => x.symbol.termRef.widen.asType
      }
      val bodyType = body.tpe.widen.asType

      captureType match {
        case '[cType] => {
          bodyType match {
            case '[bType] => {
              '{
                (param: cType) => ${
                  val subst = symGroup.map(_.symbol -> ('param).asTerm).toMap
                  substituteSymbols(subst, body).asExprOf[bType]
                }
              }.asTerm
            }
          }
        }
      }
    }

    def liftAllSymbolGroups(syms: List[List[Tree]], body: Term): Term = {
      syms match {
        case Nil    => body
        case h :: t => liftSymbolGroup(h, liftAllSymbolGroups(t, body))
      }
    }

    def groupIdentifiersByFullName(captures: List[Tree]): List[(String, List[Tree])] = {
      captures.groupBy(_.symbol.fullName).toList.sortBy(_._1)
    }

    val symbolGroup = groupIdentifiersByFullName(bodyCaptures).map(_._2)
    liftAllSymbolGroups(symbolGroup, body)
  }


  private[spores] def constructSpore0[F[_]](using Quotes)(bodyTerm: quotes.reflect.Term)(using Type[F]): Expr[Spore0[F, Any]] = {
    '{
      class Lambda extends SporeLambdaBuilder0[F, Any] {
        override def body = { ${ bodyTerm.asExpr } }
      }
      (new Lambda()).build()
    }
  }


  private[spores] def applySpore0[F[_]](using Quotes)(sporeExpr: Expr[Spore0[F, Any]], captures: List[quotes.reflect.Tree], evidence: List[quotes.reflect.Tree])(using Type[F]): Expr[Spore0[F, Any]] = {
    var tmp: Expr[Spore0[F, Any]] = sporeExpr
    captures.zip(evidence).foreach {
      (capture, evidence) => {
        val env = capture.asExpr
        val ev = evidence.asExpr
        tmp = '{
          $tmp
            .asInstanceOf[Spore0[F, Any => Any]]
            .withEnv($env)(using $ev.asInstanceOf)
        }
      }
    }
    tmp
  }


  private[spores] def spore0ApplyMacro[F[_], T](capturesExpr: Expr[Seq[Any]], bodyExpr: Expr[T])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    import quotes.reflect.*

    def deduplicateCaptures(captures: List[Tree]): List[Tree] = {
      captures.groupBy(_.symbol.fullName).toList.sortBy(_._1).map(_._2.head)
    }

    val res1 = capturesWellFormednessCheck(capturesExpr)
    val mode = res1._1

    val captureTerms = res1._2
    val captureTermsDedup = deduplicateCaptures(captureTerms)
    val bodyCaptureTerms = findCapturedIds(bodyExpr.asTerm)
    val bodyCaptureTermsDedup = deduplicateCaptures(bodyCaptureTerms)

    val evidence = findEvidence(bodyCaptureTermsDedup)

    // Mode 1: `*`: capture all by default; And mode -1: ERROR. Continue with `*` to display further errors
    if (mode == 1 || mode == -1) {
      val liftedBody = liftBody(Symbol.spliceOwner, bodyCaptureTermsDedup, bodyExpr.asTerm)
      val spore = constructSpore0[F](liftedBody)
      applySpore0(spore, bodyCaptureTermsDedup, evidence).asInstanceOf[Expr[Spore0[F, T]]]
    }
    // `()`: no capture
    else if (mode == 2) {
      checkCaptures(captureTerms, bodyCaptureTerms)
      constructSpore0(bodyExpr.asTerm).asInstanceOf[Expr[Spore0[F, T]]]
    } 
    // `(captures*)`: explicit capture list
    else { // (mode == 3)
      checkCaptures(captureTerms, bodyCaptureTerms)
      val liftedBody = liftBody(Symbol.spliceOwner, bodyCaptureTermsDedup, bodyExpr.asTerm)
      val spore = constructSpore0[F](liftedBody)
      applySpore0(spore, bodyCaptureTermsDedup, evidence).asInstanceOf[Expr[Spore0[F, T]]]
    }
  }


  private[spores] def isTopLevelObject[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be "static", i.e., top-level or defined inside a static object

    def isObject(sym: Symbol): Boolean =
      sym.flags.is(Flags.Module)

    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.maybeOwner))

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val sym = builderTpe.typeSymbol
    val owner = sym.maybeOwner

    if (!isObject(sym))
      report.error(s"The provided SporeBuilder `${sym.fullName}` is not an object.")
    if (!allOwnersOK(owner))
      report.error(s"The provided SporeBuilder `${sym.fullName}` is not a top-level object; its owner `${owner.name}` is not a top-level object nor a package.")

    '{ () }
  }


  private[spores] def isTopLevelClass[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be concrete
    // > It must have at least one public constructor
    // > It must not be a local class, i.e., defined inside a method
    //
    // In addition, we do the following checks.
    // > It is not nested in another class.
    // > It has a constructor with an empty parameter list.
    // > It has no context parameters in its parameter lists.

    def isClass(sym: Symbol): Boolean =
      sym.isClassDef && !sym.flags.is(Flags.Module)

    def isConcrete(sym: Symbol): Boolean =
      !sym.flags.is(Flags.Abstract) && !sym.flags.is(Flags.Trait) && !sym.flags.is(Flags.Sealed)

    def isPublic(sym: Symbol): Boolean =
      !sym.flags.is(Flags.Private) && !sym.flags.is(Flags.Protected)

    def isNotLocal(owner: Symbol): Boolean =
      owner.isNoSymbol || (!owner.flags.is(Flags.Method) && isNotLocal(owner.maybeOwner))

    def isNotNestedInClass(owner: Symbol): Boolean =
      owner.isNoSymbol || (!(owner.isClassDef && !owner.flags.is(Flags.Module)) && isNotNestedInClass(owner.maybeOwner))

    def containsEmptyParamList(sym: Symbol): Boolean =
      sym.paramSymss.isEmpty || sym.paramSymss.exists(_.isEmpty)

    def containsContextParamList(sym: Symbol): Boolean =
      sym.paramSymss.exists(_.exists(x => x.flags.is(Flags.Implicit) || x.flags.is(Flags.Given)))

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val sym = builderTpe.typeSymbol

    if (!isClass(sym))
      report.error(s"The provided SporeClassBuilder `${sym.fullName}` is not a class.")
    if (!isConcrete(sym))
      report.error(s"The provided SporeClassBuilder `${sym.fullName}` is not a concrete class.")
    val constructor = sym.primaryConstructor
    if (!isPublic(constructor))
      report.error(s"The provided SporeClassBuilder `${sym.fullName}` `${constructor.name}` does not have a public constructor.")
    if (!isNotLocal(sym))
      report.error(s"The provided SporeClassBuilder `${sym.fullName}` is a local class.")
    if (!isNotNestedInClass(sym.maybeOwner))
      report.error(s"The provided SporeClassBuilder `${sym.fullName}` is nested in a class.")
    if (!containsEmptyParamList(constructor))
      report.error(s"The constructor of the provided SporeClassBuilder `${sym.fullName}` `${constructor.name}` does not have an empty parameter list.")
    if (containsContextParamList(constructor))
      report.error(s"The constructor of the provided SporeClassBuilder `${sym.fullName}` `${constructor.name}` contains a context parameter list.")

    '{ () }
  }

}
