package com.syedatifakhtar.scalaterraform


import java.io.File
import java.nio.file.Files

import com.syedatifakhtar.scalaterraform.InitArguments.{BackendConfigs, HasBackend}
import com.syedatifakhtar.scalaterraform.PlanAndApplyArguments.Vars
import org.scalatest.funspec._
import org.scalatest.matchers._

import scala.reflect.io.Directory

class ScalaTerraformSpec extends AnyFunSpec with should.Matchers {

  private val sourceDirPath = getClass.getClassLoader.getResource("hello-world-tf").getPath
  private val sourceDirPathTFVars = getClass.getClassLoader.getResource("hello-world-tf-vars").getPath
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
        val tfResourceDir = sourceDirPath
        val buildFolder = new File(tempFolder.getAbsolutePath + "/build")
        InitCommand(
          tfResourceDir
          , buildFolder.getAbsolutePath
        ).run
        assertResult(true)(buildFolder.listFiles().map(_.getName).exists(_.equalsIgnoreCase(".terraform")))
        Directory(tempFolder).deleteRecursively()
      }
    }
  }

  describe("Terraform apply") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform apply -auto-approve -input=false")(ApplyCommand("", "").buildCommand)
      }
    }
    describe("is built with arguments") {
      it("should parse list of vars passed in") {
        assertResult("terraform apply -auto-approve -input=false -var='blah=blah' -var='blah2=blah'")(
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

    it("should fail when wrong var args are passed to plan") {
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



  describe("Terraform plan") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform plan -input=false")(PlanCommand("", "").buildCommand)
      }
    }
    describe("is built with arguments") {
      it("should parse list of vars passed in") {
        assertResult("terraform plan -input=false -var='blah=blah' -var='blah2=blah'")(
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

  describe("Terraform output") {
    describe("is built with empty args") {
      it("should match") {
        assertResult("terraform output -json")(OutputCommand("", "").buildCommand)
      }
    }

    describe("is run") {
      it("should capture the output as string") {
        val tempFolder = createTempDir()
        println(s"Temp folder: ${tempFolder}")
        val tfResourceDir = new File(sourceDirPath)
        val buildDirFile = new File(tempFolder.getAbsolutePath + "/build")
        val output = for {
          _ <- InitCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
            , HasBackend()
            , BackendConfigs(Map("path" -> "someKey.tfstate"))
          ).run
          _ <- ApplyCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath)
            .run
          result <- OutputCommand(
            tfResourceDir.getAbsolutePath
            , buildDirFile.getAbsolutePath
          ).run
        } yield (result)
        assertResult("Hello, World!")(output.get("hello_world"))
        Directory(tempFolder).deleteRecursively()
      }
    }
  }

}
