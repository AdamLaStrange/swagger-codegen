import com.wordnik.swagger.codegen.BasicGenerator

import com.wordnik.swagger.core._

import java.io.File

object PythonCodegen extends PythonCodegen {
  def main(args: Array[String]) = generateClient(args)
}

class PythonCodegen extends BasicGenerator {
  // set imports for common datatypes
  override def imports = Map()

  // template used for models
  modelTemplateFiles += "model.mustache" -> ".py"

  // template used for models
  apiTemplateFiles += "api.mustache" -> ".py"

  // location of templates
  override def templateDir = "src/main/resources/python"

  // where to write generated code
  override def destinationDir = "generated-code/python"

  // package for models
  override def modelPackage = Some("models")

  // package for apis
  override def apiPackage = Some("")

  // file suffix
  override def fileSuffix = ".py"

  // reserved words which need special quoting
  // These will all be object properties, in which context we don't need
  // to worry about escaping them for Python.
  override def reservedWords = Set()

  // import/require statements for specific datatypes
  override def importMapping = Map()


 // response classes
  override def processResponseClass(responseClass: String): Option[String] = {
    typeMapping.contains(responseClass) match {
      case true => Some(typeMapping(responseClass))
      case false => {
        responseClass match {
          case "void" => None
          case e: String => {
            responseClass.startsWith("List") match {
              case true => Some("list")
              case false => Some(responseClass)
            }
          }
        }
      }
    }
  }


  override def processResponseDeclaration(responseClass: String): Option[String] = {
    typeMapping.contains(responseClass) match {
      case true => Some(typeMapping(responseClass))
      case false => {
        responseClass match {
          case "void" => None
          case e: String => {
            responseClass.startsWith("List") match {
              case true => {
                val responseSubClass = responseClass.dropRight(1).substring(5)
                typeMapping.contains(responseSubClass) match {
                  case true => Some("list[" + typeMapping(responseSubClass) + "]")
                  case false => Some("list[" + responseSubClass + "]")
                }
              }
              case false => Some(responseClass)
            }
          }
        }
      }
    }
  }
  override def typeMapping = Map(
    "String" -> "str",
    "Int" -> "int",
    "Float" -> "float",
    "Long" -> "long",
    "Double" -> "float",
    "Array" -> "list",
    "Boolean" -> "bool",
    "Date" -> "str")

  override def toDeclaredType(dt: String): String = {
    typeMapping.getOrElse(dt, dt)
  }

  override def toDeclaration(obj: DocumentationSchema) = {
    var declaredType = toDeclaredType(obj.getType)

    declaredType match {
      case "Array" => {
        declaredType = "list"
      }
      case e: String => e
    }

    val defaultValue = toDefaultValue(declaredType, obj)
    declaredType match {
      case "list" => {
        val inner = {
          if (obj.items.ref != null) obj.items.ref
          else toDeclaredType(obj.items.getType)
        }
        declaredType += "[" + inner + "]"
        "list"
      }
      case _ =>
    }
    (declaredType, defaultValue)
  }

  // escape keywords
  override def escapeReservedWord(word: String) = "`" + word + "`"

  // supporting classes
  override def supportingFiles = List(
    ("__init__.mustache", destinationDir, "__init__.py"),
    ("swagger.mustache", destinationDir + File.separator + apiPackage.get,
     "swagger.py"),
    ("__init__.mustache", destinationDir + File.separator +
     modelPackage.get, "__init__.py"))
}
