package com.syedatifakhtar.utils

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils

import java.io.File
import java.net.{FileNameMap, JarURLConnection}
import scala.jdk.CollectionConverters._

object FileResourceUtil extends LazyLogging{

  def removeStart(fileName: String, substringToRemove: String) = {
    if (fileName.isEmpty || substringToRemove.isEmpty) fileName
    else if (fileName.equals(substringToRemove)) fileName
    else if (fileName.startsWith(substringToRemove)) fileName.substring(substringToRemove.length)
    else fileName
  }

  def copyJarResourcesRecursively(destDir: File, jarURLConnection: JarURLConnection): Boolean = {
    val jarFile = jarURLConnection.getJarFile
    jarFile.entries().asScala.toSeq.foreach{
      entry =>
        entry.getName.startsWith(jarURLConnection.getEntryName)
        if(entry.getName.startsWith(jarURLConnection.getEntryName)) {
          val fileName = removeStart(entry.getName,jarURLConnection.getEntryName)
          val f = new File(destDir,fileName)
          if(!entry.isDirectory) {
            val entryInputStream = jarFile.getInputStream(entry)
            FileUtils.copyInputStreamToFile(entryInputStream, f)
            entryInputStream.close()
          }
          else {
            logger.debug(s"Creating directory ${f.getAbsolutePath}")
            FileUtils.forceMkdir(f)
          }
        }
    }
    true
  }
}