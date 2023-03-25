plugins { id("sh.eliza.monocleimage.kotlin-application-conventions") }

dependencies { implementation(project(":lib")) }

application {
  // Define the main class for the application.
  mainClass.set("sh.eliza.monocleimage.app.AppKt")
}
