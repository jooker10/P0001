package anouar.oulhaj.p001;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

import anouar.oulhaj.p001.QuizFrags.QuizCategoriesFragment;
import anouar.oulhaj.p001.navfragments.SettingsNavFragment;


public class InfoQuizFragment extends Fragment {

    Button btnVerbs;
    Button btnSentences;
    Button btnPhrasals;
    Button btnNouns;
    Button btnAdjs;
    Button btnAdvs;
    Button btnIdioms;

    setFragmentToReplaceOnClickListener listener;


    public InfoQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof setFragmentToReplaceOnClickListener){
            listener = (setFragmentToReplaceOnClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_quiz, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnVerbs = view.findViewById(R.id.btnVerbs);
        btnSentences = view.findViewById(R.id.btnSentences);
        btnPhrasals = view.findViewById(R.id.btnPhrasals);
        btnNouns = view.findViewById(R.id.btnNouns);
        btnAdjs = view.findViewById(R.id.btnAdjs);
        btnAdvs = view.findViewById(R.id.btnAdvs);
        btnIdioms = view.findViewById(R.id.btnIdioms);
        btnVerbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.VERB_NAME));
            }
        });
        btnVerbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.VERB_NAME));
            }
        });
        btnSentences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.SENTENCE_NAME));
            }
        });
        btnPhrasals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.PHRASAL_NAME));
            }
        });
        btnNouns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.NOUN_NAME));
            }
        });
        btnAdjs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.ADJ_NAME));
            }
        });
        btnAdvs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.ADV_NAME));
            }
        });
        btnIdioms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.Replace(QuizCategoriesFragment.getInstance(Constants.IDIOM_NAME));
            }
        });
    }


    public interface setFragmentToReplaceOnClickListener{
        void Replace(Fragment fragment);
    }

}