# Scala Wrapper for Terraform

### Why
 
1.People usually split repository for Terraform and Scala, this allows you to keep both in the same respository.I can create entire Infra and code needed from single repo running a single SBT/Gradle doAll task

2.It allows you to have central configs ie I can use HOCON style single config to create Infra and use in my spark jobs. Hence S3 bucket/EMR cluster name in Infra is always in sync.I can also pass in arguments based on environment rather than creating multiple TFvars

3.Scala as DSL layer -: I can mix and match Infra ie when running SBT I can decide if I want to create and run my app on AWS vs GCP vs Azure, can create or not create components directly, create modules in loop

### How to Use

Clone the project and run the following: 

``sbt publish local``

Include in your project as ``"com.syedatifakhtar" %% "scalaterraform" %"0.1-SNAPSHOT"``
