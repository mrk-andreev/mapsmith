plugins { application }

dependencies { implementation(project(":mapsmith-core")) }

application { mainClass = "name.mrkandreev.mapsmith.samples.Main" }
