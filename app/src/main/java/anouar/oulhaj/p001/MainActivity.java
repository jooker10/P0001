package anouar.oulhaj.p001;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import anouar.oulhaj.p001.DB.DbAccess;
import anouar.oulhaj.p001.DB.Verb;
import anouar.oulhaj.p001.QuizFrags.ChoicesSentencesQcmFrag;
import anouar.oulhaj.p001.navfragments.QuizNavFragContainer;
import anouar.oulhaj.p001.navfragments.HomeNavFragment;
import anouar.oulhaj.p001.navfragments.TablesNavFragments;

public class MainActivity extends AppCompatActivity implements DialogFragment.onDialogPositiveClickListener
, DialogFragment.onDialogNegativeClickListener, HomeNavFragment.HomeFragClickListener, DialogFragment.onDialogNeutralClickListener, MyBottomSheet.SheetItemClickListener, ChoicesSentencesQcmFrag.setOnChoicesFragClickListener {

    // -------Declaration of variables------------
    BottomNavigationView bottom_nav;
    FloatingActionButton main_fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----view inflate-------
        bottom_nav = findViewById(R.id.bottom_nav);
        main_fab = findViewById(R.id.main_nav_fab);

        setBottomNavWithMenu();

        main_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBottomSheet();
            }
        });

    }

    //-----------------------for Bottom Navigation view------------------------------
    private void setBottomNavWithMenu(){
        //----SetMenuItem and His Frag-------------
        setNavFragment(new HomeNavFragment());
        bottom_nav.getMenu().getItem(0).setChecked(true);
        bottom_nav.setOnItemSelectedListener(item -> {

            switch(item.getItemId()){
                case R.id.item_nav_home:
                    setNavFragment(new HomeNavFragment());
                    return true;
                case R.id.item_nav_tables:
                    setNavFragment(new TablesNavFragments());
                    return true;
                case R.id.item_nav_quiz:
                    setNavFragment(new QuizNavFragContainer());
                    return true;
                case R.id.item_nav_settings:
                    setNavFragment(new SettingsFragment());
                    return true;
            }
            return false;
        });
    }

    private void ShowBottomSheet() {
          MyBottomSheet myBottomSheet = MyBottomSheet.newInstance();
          myBottomSheet.show(getSupportFragmentManager(),MyBottomSheet.SHEET_TAG);
    }

    private void setNavFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_frag_container,fragment);
        ft.commit();
    }

    @Override
    public void onDialogPositiveClick(String fr, String eng) {
       //------Btn to Insert Data to DB----------------
        DbAccess db = DbAccess.getInstance(this);
        db.open_to_write();
        db.InsertVerbs(new Verb(fr,eng));
        db.close();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    @Override
    public void onDialogNeutralClick() {

    }

    @Override
    public void sheetBtnClick() {
        Toast.makeText(this, "Button Sheet clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sheetItemClicked(String str) {
        Toast.makeText(this, "sheet "+str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setScoreClick(int s0,int s1,int s2) {
        HomeNavFragment homeNavFragment = HomeNavFragment.newInstance(s0,s1,s2);
        setNavFragment(homeNavFragment);
        bottom_nav.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void reLoadFragment(String category) {

    }

    @Override
    public void onHomeGetStarted() {
        setNavFragment(new TablesNavFragments());
        bottom_nav.getMenu().getItem(1).setChecked(true);
    }
    //--------------------------------*****------------------------------------------
}