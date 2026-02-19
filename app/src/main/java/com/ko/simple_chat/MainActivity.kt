package com.ko.simple_chat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ko.simple_chat.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

//        enableEdgeToEdge()
        setContentView(binding.root)
        checkAndRequestPermissions()

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val locationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val albumGranted =
                permissions[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true

            if (locationGranted && albumGranted) {
                Toast.makeText(this, "모든 권한 허용됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "일부 권한이 거부됨", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkAndRequestPermissions() {
        val permissionList = mutableListOf<String>()

        // 위치 권한
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 앨범 권한 (버전 분기)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionList.isNotEmpty()) {
            showPermissionDialog(permissionList.toTypedArray())
        }
    }

    // 사용자에게 설명 팝업
    private fun showPermissionDialog(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("권한 요청")
            .setMessage(
                "사진, 위치 정보는 앱 사용 중에만 필요합니다.\n" +
                        "원활한 앱 사용을 위해 권한을 허용해주세요."
            )
            .setPositiveButton("허용") { _, _ ->
                permissionLauncher.launch(permissions)
            }
            .setNegativeButton("거부") { _, _ -> }
            .show()
    }
}