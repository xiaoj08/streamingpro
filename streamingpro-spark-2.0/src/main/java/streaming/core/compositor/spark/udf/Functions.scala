package streaming.core.compositor.spark.udf

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{Row, UDFRegistration}

import scala.collection.JavaConversions._
import scala.collection.mutable
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.mllib.linalg.{Vectors => OldVectors}
import org.apache.spark.sql.functions._

/**
  * Created by allwefantasy on 3/5/2017.
  */
object Functions {
  def parse(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("parse", (co: String) => {
      val parseMethod = Class.forName("org.ansj.splitWord.analysis.NlpAnalysis").getMethod("parse", classOf[String])
      val tmp = parseMethod.invoke(null, co)
      val terms = tmp.getClass.getMethod("getTerms").invoke(tmp).asInstanceOf[java.util.List[Any]]
      terms.map(f => f.asInstanceOf[ {def getName: String}].getName).toArray
    })
  }

  def mkString(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("mkString", (sep: String, co: mutable.WrappedArray[String]) => {
      co.mkString(sep)
    })
  }

  def sleep(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("sleep", (sleep: Long) => {
      Thread.sleep(sleep)
      ""
    })
  }

  def vec_argmax(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_argmax", (vector: Vector) => {
      vector.argmax
    })
  }

  def vec_sqdist(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_sqdist", (vec1: Vector, vec2: Vector) => {
      Vectors.sqdist(vec1, vec2)
    })
  }

  def vec_norm(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_norm", (vec1: Vector, p: Double) => {
      Vectors.norm(vec1, p)
    })
  }

  def vec_dense(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_dense", (vec1: Seq[Double]) => {
      Vectors.dense(vec1.toArray)
    })
  }

  def vec_sparse(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_sparse", (size: Int, vec1: Map[Int, Double]) => {
      Vectors.sparse(size, vec1.toSeq)
    })
  }

  def vec_concat(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_concat", (vecs: Seq[Vector]) => {
      FVectors.assemble(vecs: _*)
    })
  }

  /*
    1 - x.dot(y)/(x.norm(2)*y.norm(2))
   */
  def vec_cosine(uDFRegistration: UDFRegistration) = {
    uDFRegistration.register("vec_cosine", (vec1: Vector, vec2: Vector) => {
      val dot = new org.apache.spark.mllib.feature.ElementwiseProduct(OldVectors.fromML(vec1)).transform(OldVectors.fromML(vec2))
      var value = 0d
      val value_add = (a: Int, b: Double) => {
        value += b
      }
      dot.foreachActive(value_add)
      value / (Vectors.norm(vec1, 2) * Vectors.norm(vec2, 2))
    })
  }
}
