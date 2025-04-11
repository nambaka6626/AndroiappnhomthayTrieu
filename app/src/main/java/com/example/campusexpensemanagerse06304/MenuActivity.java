package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campusexpensemanagerse06304.adapter.ViewPagerAdapter;
import com.example.campusexpensemanagerse06304.database.BudgetDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    private BudgetDb budgetDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Khởi tạo các view
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager2 = findViewById(R.id.viewPager);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem logout = menu.findItem(R.id.nav_logout);

        // Thiết lập dữ liệu mẫu
        setDataBudget();

        // Thiết lập ViewPager
        setupViewPager();

        // bat su kien logout
        logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intentLogout = new Intent(MenuActivity.this, SignInActivity.class);
                startActivity(intentLogout);
                finish();
                return false;
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home){
                viewPager2.setCurrentItem(0);
            } else if (item.getItemId() == R.id.menu_expense) {
                viewPager2.setCurrentItem(1);
            } else if (item.getItemId() == R.id.menu_budget) {
                viewPager2.setCurrentItem(2);
            } else if (item.getItemId() == R.id.menu_setting) {
                viewPager2.setCurrentItem(3);
            } else if (item.getItemId() == R.id.menu_overview) {
                viewPager2.setCurrentItem(4);
            }
            return true;
        });
    }

    private void setupViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0){
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_expense).setChecked(true);
                } else if (position == 2) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_budget).setChecked(true);
                } else if (position == 3) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_setting).setChecked(true);
                } else if (position == 4) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_overview).setChecked(true);
                } else {
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_home){
            viewPager2.setCurrentItem(0);
        } else if (item.getItemId() == R.id.menu_expense) {
            viewPager2.setCurrentItem(1);
        } else if (item.getItemId() == R.id.menu_budget) {
            viewPager2.setCurrentItem(2);
        } else if (item.getItemId() == R.id.menu_setting) {
            viewPager2.setCurrentItem(3);
        } else if (item.getItemId() == R.id.menu_overview) {
            viewPager2.setCurrentItem(4);
        } else {
            viewPager2.setCurrentItem(0);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDataBudget() {
        budgetDb = new BudgetDb(this);
        List<Budget> existingBudgets = budgetDb.getAllBudgets();

        // Chỉ thêm dữ liệu mẫu nếu chưa có dữ liệu
        if (existingBudgets.isEmpty()) {
            budgetDb.insertBudget("Ăn uống", 2000000, 1800000);
            budgetDb.insertBudget("Giải trí", 1000000, 500000);
            budgetDb.insertBudget("Di chuyển", 800000, 900000);
            budgetDb.insertBudget("Khác", 500000, 200000);

        }
    }
}
//test
