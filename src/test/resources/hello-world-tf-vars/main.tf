terraform {
  backend "local" {}
}

variable "firstname" { type = "string"}
variable "lastname" { type = "string"}
output "hello_world" {
  value = "Hello, ${var.firstname} ${var.lastname}"
}