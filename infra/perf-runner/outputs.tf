output "instance_id" {
  description = "ID of the Spot-backed performance runner instance."
  value       = aws_instance.perf_runner.id
}

output "public_ip" {
  description = "Public IPv4 address, if one was assigned."
  value       = aws_instance.perf_runner.public_ip
}
