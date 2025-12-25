package com.example.demolition

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var ivUserProfile: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var realtimeDB: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private var userProfileListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottom_nav)

        // Fix system insets for status bar/notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        realtimeDB = FirebaseDatabase.getInstance()

        // Initialize Firebase services
        initializeFirebaseServices()

        tvUserName = findViewById(R.id.tv_username)
        ivUserProfile = findViewById(R.id.ivToolbarPic)

        setupDrawerMenu()
        setupBottomNavigation()
        setupFab()
        loadUserProfile()
        setupBackPressHandler()

        // Show Home on startup
        loadFragment(Home())
    }

    /**
     * Initialize Firebase Crashlytics and other monitoring services
     */
    private fun initializeFirebaseServices() {
        try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize Firebase services", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFab() {
        val fabAiChat = findViewById<FloatingActionButton>(R.id.fab_ai_chat)
        val deleteOverlay = findViewById<android.widget.FrameLayout>(R.id.deleteZoneOverlay)
        val deleteZone = findViewById<android.widget.LinearLayout>(R.id.deleteZone)
        
        var dX = 0f
        var dY = 0f
        var lastAction = 0
        var startClickTime = 0L
        var isDragging = false
        
        fabAiChat.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    lastAction = android.view.MotionEvent.ACTION_DOWN
                    startClickTime = System.currentTimeMillis()
                    isDragging = false
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    // Show delete overlay when dragging starts
                    if (!isDragging) {
                        isDragging = true
                        deleteOverlay.visibility = android.view.View.VISIBLE
                    }
                    
                    // Calculate new position
                    var newX = event.rawX + dX
                    var newY = event.rawY + dY
                    
                    // Constrain to parent bounds
                    val parent = view.parent as android.view.View
                    newX = newX.coerceIn(0f, (parent.width - view.width).toFloat())
                    newY = newY.coerceIn(0f, (parent.height - view.height).toFloat())
                    
                    view.x = newX
                    view.y = newY
                    
                    // Check if FAB is over delete zone
                    val deleteLocation = IntArray(2)
                    deleteZone.getLocationOnScreen(deleteLocation)
                    val fabCenterX = event.rawX
                    val fabCenterY = event.rawY
                    
                    val isOverDelete = fabCenterX >= deleteLocation[0] && 
                                       fabCenterX <= deleteLocation[0] + deleteZone.width &&
                                       fabCenterY >= deleteLocation[1] && 
                                       fabCenterY <= deleteLocation[1] + deleteZone.height
                    
                    // Highlight delete zone
                    if (isOverDelete) {
                        deleteZone.setBackgroundResource(R.drawable.delete_zone_bg_highlight)
                        deleteZone.scaleX = 1.2f
                        deleteZone.scaleY = 1.2f
                    } else {
                        deleteZone.setBackgroundResource(R.drawable.delete_zone_bg)
                        deleteZone.scaleX = 1.0f
                        deleteZone.scaleY = 1.0f
                    }
                    
                    lastAction = android.view.MotionEvent.ACTION_MOVE
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    // Hide overlay
                    deleteOverlay.visibility = android.view.View.GONE
                    deleteZone.scaleX = 1.0f
                    deleteZone.scaleY = 1.0f
                    
                    // Check if dropped on delete zone
                    val deleteLocation = IntArray(2)
                    deleteZone.getLocationOnScreen(deleteLocation)
                    val fabCenterX = event.rawX
                    val fabCenterY = event.rawY
                    
                    val isOverDelete = fabCenterX >= deleteLocation[0] && 
                                       fabCenterX <= deleteLocation[0] + deleteZone.width &&
                                       fabCenterY >= deleteLocation[1] && 
                                       fabCenterY <= deleteLocation[1] + deleteZone.height
                    
                    if (isOverDelete && isDragging) {
                        // Hide FAB
                        view.visibility = android.view.View.GONE
                        android.widget.Toast.makeText(this, "AI button hidden. Restart app to show again.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val clickDuration = System.currentTimeMillis() - startClickTime
                        
                        // Short tap to open AI chat (not during drag)
                        if (!isDragging && clickDuration < 300) {
                            loadFragment(AiChatterFrag())
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    fun showFab() {
        findViewById<FloatingActionButton>(R.id.fab_ai_chat).visibility = android.view.View.VISIBLE
    }
    
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Let system handle back (exit app)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupDrawerMenu() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)

            val id = item.itemId

            if (id == R.id.nav_home) loadFragment(Home())
            else if (id == R.id.nav_courses) loadFragment(Courses())
//            else if (id == R.id.nav_progress) loadFragment(Progress())
            else if (id == R.id.nav_profile) loadFragment(Profile())

            true
        }
    }

    /**
     * BOTTOM NAVIGATION
     */
    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(Home())
                R.id.nav_courses -> loadFragment(Courses())
//                R.id.nav_ai -> loadFragment(AiChatterFrag())
                R.id.nav_profile -> loadFragment(Profile())
            }
            true
        }
    }

    /**
     * FRAGMENT LOADER
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    /**
     * LOAD USER PROFILE FROM REALTIME DATABASE
     */
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = realtimeDB.getReference("Users/$userId")

        userProfileListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    
                    user?.let {
                        // Input validation
                        val sanitizedName = it.name.take(50).trim()
                        
                        // Display user's full name
                        tvUserName.text = sanitizedName.ifEmpty { 
                            "${it.firstName.take(25).trim()} ${it.lastName.take(25).trim()}".trim()
                        }

                        // Display selected avatar with validation
                        if (it.avatarId.isNotEmpty() && it.avatarId.matches(Regex("^[a-z0-9_]+$"))) {
                            val avatarRes = resources.getIdentifier(
                                it.avatarId,
                                "drawable",
                                packageName
                            )
                            if (avatarRes != 0) {
                                ivUserProfile.setImageResource(avatarRes)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainActivity", "Failed to load user profile", error.toException())
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(error.toException())
            }
        }
        
        userRef.addValueEventListener(userProfileListener!!)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up Firebase listener to prevent memory leak
        userProfileListener?.let {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                realtimeDB.getReference("Users/$userId").removeEventListener(it)
            }
        }
    }
}
