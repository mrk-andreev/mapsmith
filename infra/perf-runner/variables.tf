variable "repository" {
  description = "GitHub repository that owns the self-hosted runner."
  type        = string
  default     = "mrk-andreev/mapsmith"
}

variable "aws_region" {
  description = "AWS region in which to create the performance runner."
  type        = string
  default     = "eu-west-2"
}

variable "instance_type" {
  description = "EC2 instance type for the performance runner."
  type        = string
  default     = "c7i.xlarge"
}

variable "subnet_id" {
  description = "Subnet in the selected AWS region in which to create the runner. Leave null to use the account default VPC subnet."
  type        = string
  default     = null
}

variable "security_group_ids" {
  description = "Security groups for the runner. No inbound rules are required; it needs outbound HTTPS access."
  type        = list(string)
  default     = null
}

variable "associate_public_ip_address" {
  description = "Whether the runner receives a public IPv4 address. Set false when the subnet has NAT egress."
  type        = bool
  default     = true
}

variable "tags" {
  description = "Additional AWS tags."
  type        = map(string)
  default     = {}
}
