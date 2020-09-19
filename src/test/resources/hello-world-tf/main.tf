terraform {
  backend "local" {}
}

output "hello_world" {
  value = "Hello, World!"
}