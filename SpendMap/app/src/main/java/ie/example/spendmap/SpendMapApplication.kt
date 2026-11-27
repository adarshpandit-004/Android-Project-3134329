
package ie.example.spendmap

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class SpendMapApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Give the map server a clear app identifier instead of using
        // a generic default User-Agent.
        Configuration.getInstance().userAgentValue = packageName

        // Keep map files inside SpendMap's private app storage.
        // Android manages this folder, so no extra storage permission is needed.
        val mapFolder = File(filesDir, "osmdroid")
        val tileFolder = File(mapFolder, "tiles")

        // Create the folders if they do not already exist.
        mapFolder.mkdirs()
        tileFolder.mkdirs()

        // Tell OSMDroid where to store downloaded map tiles.
        Configuration.getInstance().osmdroidBasePath = mapFolder
        Configuration.getInstance().osmdroidTileCache = tileFolder
    }
}
