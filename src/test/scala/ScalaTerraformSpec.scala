
import java.io.File
import java.nio.file.Files

import com.scalaterraform.commands.PlanAndApplyArguments.Vars
import com.scalaterraform.commands.{ApplyCommand, PlanCommand}
import com.scalaterraform.commands.InitArguments._
import org.scalatest.funspec._
import org.scalatest.matchers._

import scala.reflect.io.Directory

class ScalaTerraformSpec extends AnyFunSpec with should.Matchers {

  private val sourceDirPath = getClass.getResource("hello-world-tf").getPath
  private val sourceDirPathTFVars = getClass.getResource("hello-world-tf-vars").getPath
  def createTempDir(): File = {
    Files.createTempDirectory("tmp").toFile
  }

  describe("Terraform Init") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform init")(InitCommand("", "").buildCommand)
      }
    }
    describe("is built with single backend config") {
      it("should run with single backend config value") {
        assertResult("terraform init -backend=true -backend-config='a=b'")(InitCommand("", "", HasBackend(), BackendConfigs(Map("a" -> "b"))).buildCommand)
      }
    }
    describe("is built with multiple backend config") {
      it("should run with single backend config value") {
        assertResult("terraform init -backend=true -backend-config='a=b' -backend-config='c=d'")(InitCommand("", "",
          HasBackend(),
          BackendConfigs(Map("a" -> "b", "c" -> "d"))).buildCommand)
      }
    }
    describe("is run with some args") {
      it("should trigger shell process and create the terraform directory") {
        val tempFolder = createTempDir
        println(s"Temp folder: ${tempFolder}")
        val tfResourceDir = new File(getClass.getResource("hello-world-tf").getPath)
        val buildFolder = new File(tempFolder.getAbsolutePath + "/build")
        InitCommand(
          tfResourceDir.getAbsolutePath
          , buildFolder.getAbsolutePath
        ).run
        assertResult(true)(buildFolder.listFiles().map(_.getName).exists(_.equalsIgnoreCase(".terraform")))
//        Directory(tempFolder).deleteRecursively()
      }
    }
  }

  describe("Terraform apply") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform apply -auto-approve")(ApplyCommand("", "").buildCommand)
      }
    }
    describe("is built with arguments") {
      it("should parse list of vars passed in") {
        assertResult("terraform apply -auto-approve -var='blah=blah' -var='blah2=blah'")(
          ApplyCommand("", "", Vars(Map("blah" -> "blah", "blah2" -> "blah"))).buildCommand)
      }
    }
    describe("is run") {
      it("create resources when no args are passed") {
        val tempFolder = createTempDir()
        println(s"Temp folder: ${tempFolder}")
        val tfResourceDir = new File(sourceDirPath)
        val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
        InitCommand(
          tfResourceDir.getAbsolutePath
          , buildDirFile.getAbsolutePath
          , HasBackend()
          , BackendConfigs(Map("path" -> "someKey.tfstate"))
        ).run
          .flatMap { _ =>
            ApplyCommand(
              tfResourceDir.getAbsolutePath
              , buildDirFile.getAbsolutePath)
              .run
          }
        assertResult(true)(
          buildDirFile
            .listFiles()
            .map(_.getName)
            .exists(_.equalsIgnoreCase("someKey.tfstate")))
        Directory(tempFolder).deleteRecursively()
      }
    }

    it("should create resources when var args are passed") {
      val tempFolder = createTempDir()
      println(s"Temp folder: ${tempFolder}")
      val tfResourceDir = new File(sourceDirPathTFVars)
      val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
      InitCommand(
        tfResourceDir.getAbsolutePath
        , buildDirFile.getAbsolutePath
        , HasBackend()
        , BackendConfigs(Map("path" -> "someKey.tfstate"))
      ).run
        .flatMap { _ =>
          ApplyCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
            , Vars(Map(
              "firstname"->"blah1",
              "lastname"->"blah2"
            )))
            .run
        }
      assertResult(true)(
        buildDirFile
          .listFiles()
          .map(_.getName)
          .exists(_.equalsIgnoreCase("someKey.tfstate")))
      Directory(tempFolder).deleteRecursively()
    }

    it("should fail when wrong var args are passed") {
      val tempFolder = createTempDir()
      println(s"Temp folder: ${tempFolder}")
      val tfResourceDir = new File(sourceDirPathTFVars)
      val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
      InitCommand(
        tfResourceDir.getAbsolutePath
        , buildDirFile.getAbsolutePath
        , HasBackend()
        , BackendConfigs(Map("path" -> "someKey.tfstate"))
      ).run
        .flatMap { _ =>
          ApplyCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
            , Vars(Map(
              "blah"->"blah1",
              "lastname"->"blah2"
            )))
            .run
        }
      assertResult(false)(
        buildDirFile
          .listFiles()
          .map(_.getName)
          .exists(_.equalsIgnoreCase("someKey.tfstate")))
      Directory(tempFolder).deleteRecursively()
    }
  }

  describe("Terraform plan") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform plan")(PlanCommand("", "").buildCommand)
      }
    }
    describe("is built with arguments") {
      it("should parse list of vars passed in") {
        assertResult("terraform plan -var='blah=blah' -var='blah2=blah'")(
          PlanCommand("", "", Vars(Map("blah" -> "blah", "blah2" -> "blah"))).buildCommand)
      }
    }
    describe("is run") {
      it("should plan resources when no args are passed") {
        val tempFolder = createTempDir()
        println(s"Temp folder: ${tempFolder}")
        val tfResourceDir = new File(sourceDirPath)
        val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
        InitCommand(
          tfResourceDir.getAbsolutePath
          , buildDirFile.getAbsolutePath
          , HasBackend()
          , BackendConfigs(Map("path" -> "someKey.tfstate"))
        ).run
          .flatMap { _ =>
            PlanCommand(
              tfResourceDir.getAbsolutePath
              , buildDirFile.getAbsolutePath)
              .run
          }
        assertResult(false)(
          buildDirFile
            .listFiles()
            .map(_.getName)
            .exists(_.equalsIgnoreCase("someKey.tfstate")))
        Directory(tempFolder).deleteRecursively()
      }
    }

    it("should plan resources when var args are passed") {
      val tempFolder = createTempDir()
      println(s"Temp folder: ${tempFolder}")
      val tfResourceDir = new File(sourceDirPathTFVars)
      val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
      InitCommand(
        tfResourceDir.getAbsolutePath
        , buildDirFile.getAbsolutePath
        , HasBackend()
        , BackendConfigs(Map("path" -> "someKey.tfstate"))
      ).run
        .flatMap { _ =>
          PlanCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
            , Vars(Map(
              "firstname"->"blah1",
              "lastname"->"blah2"
            )))
            .run
        }
      assertResult(false)(
        buildDirFile
          .listFiles()
          .map(_.getName)
          .exists(_.equalsIgnoreCase("someKey.tfstate")))
      Directory(tempFolder).deleteRecursively()
    }

    it("should fail when wrong var args are passed") {
      val tempFolder = createTempDir()
      println(s"Temp folder: ${tempFolder}")
      val tfResourceDir = new File(sourceDirPathTFVars)
      val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
      InitCommand(
        tfResourceDir.getAbsolutePath
        , buildDirFile.getAbsolutePath
        , HasBackend()
        , BackendConfigs(Map("path" -> "someKey.tfstate"))
      ).run
        .flatMap { _ =>
          PlanCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
            , Vars(Map(
              "blah"->"blah1",
              "lastname"->"blah2"
            )))
            .run
        }
      assertResult(false)(
        buildDirFile
          .listFiles()
          .map(_.getName)
          .exists(_.equalsIgnoreCase("someKey.tfstate")))
      Directory(tempFolder).deleteRecursively()
    }
  }

}
