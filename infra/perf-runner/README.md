# Mapsmith performance runner

This Terraform configuration creates a `c7i.xlarge` one-time Spot instance
(default region: `eu-west-2`) and configures it as a GitHub Actions
self-hosted runner for `mrk-andreev/mapsmith`, labelled `mapsmith-perf`.

Prerequisites:

- Terraform 1.5 or newer and AWS credentials for the selected region.
- GitHub CLI authenticated locally with permission to administer Actions
  runners for the repository (`gh auth login`).
- A subnet with outbound HTTPS access. The default configuration uses a public
  IP; set `associate_public_ip_address = false` when using a private subnet
  with NAT egress.

Run it from this directory:

```sh
terraform init
terraform apply
```

Specify network placement when the account does not have a suitable default VPC:

```sh
terraform apply -var='subnet_id=subnet-0123456789abcdef0'
```

Override the default `c7i.xlarge` instance type when needed:

```sh
terraform apply -var='instance_type=c7i.2xlarge'
```

Override the default `eu-west-2` region when needed:

```sh
terraform apply -var='aws_region=eu-central-1'
```

Destroy the instance when benchmarks are finished:

```sh
terraform destroy
```

To recreate a runner deliberately, request replacement so Terraform obtains a
fresh registration token:

```sh
terraform apply -replace=aws_instance.perf_runner
```

The registration token is deliberately obtained through `gh` at apply time.
Terraform records the resulting user data in its local state, so keep the
state file private and do not commit it.
