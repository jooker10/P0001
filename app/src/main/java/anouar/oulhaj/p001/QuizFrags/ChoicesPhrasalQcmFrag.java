package anouar.oulhaj.p001.QuizFrags;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import anouar.oulhaj.p001.DB.DbAccess;
import anouar.oulhaj.p001.DB.Phrasal;
import anouar.oulhaj.p001.Question;
import anouar.oulhaj.p001.R;


public class ChoicesPhrasalQcmFrag extends Fragment {

    private setOnChoicesFragClickListener listener;

    private TextView tv_qst, tv_score, tv_qstCount, tv_ofThePhrasal, tv_notes;
    private RadioGroup radioGroup_choices;
    private RadioButton rb0, rb1, rb2;
    private Button btn_confirmNext;

    private ColorStateList rb_defaultColor_txt;

    private List<Question> qstsList = new ArrayList<>();
    private List<Phrasal> AllPhrasal = new ArrayList<>();
    private int qst_counter, qstCounterTotal;
    private int score_phrasal;

    private boolean isAnswered;

    Question currentQuestion;


    public ChoicesPhrasalQcmFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choices_phrasal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_qst = view.findViewById(R.id.choicesPhrasal_TVQst);
        tv_score = view.findViewById(R.id.choicesPhrasal_tv_score);
        tv_qstCount = view.findViewById(R.id.choicesPhrasal_tv_qstNumber);
        tv_ofThePhrasal = view.findViewById(R.id.choicesPhrasal_theVerbQst);
        tv_notes = view.findViewById(R.id.choicesPhrasal_tv_notes);

        btn_confirmNext = view.findViewById(R.id.choicesPhrasal_Btn_confirmNext);

        radioGroup_choices = view.findViewById(R.id.choicesPhrasal_radioGroup_choices);
        rb0 = view.findViewById(R.id.choicesPhrasal_option0);
        rb1 = view.findViewById(R.id.choicesPhrasal_option1);
        rb2 = view.findViewById(R.id.choicesPhrasal_option2);


        rb_defaultColor_txt = rb1.getTextColors();
        //------- fill Lists--------------
        DbAccess db = DbAccess.getInstance(getActivity());
        db.open_to_read();
        AllPhrasal = db.getAllPhrasal();
        db.close();

        for (int i = 0; i < AllPhrasal.size(); i++) {
            List<Integer> randomList = new ArrayList<>();
            Random random = new Random();

            while(true) {
                int j = random.nextInt(AllPhrasal.size());
                if(!randomList.contains(j) && j != i){
                    randomList.add(j);
                }
                if(randomList.size() == 2) break;
            }

            randomList.add(i);
            Collections.shuffle(randomList);

            String qst = getString(R.string.qstPhrasalIntro);
            String rb0_str = AllPhrasal.get(randomList.get(0)).getGetPhrasal_eng();
            String rb1_str = AllPhrasal.get(randomList.get(1)).getGetPhrasal_eng();
            String rb2_str = AllPhrasal.get(randomList.get(2)).getGetPhrasal_eng();
            String right_Answer = AllPhrasal.get(i).getGetPhrasal_eng();

            qstsList.add(new Question(qst, rb0_str, rb1_str, rb2_str, right_Answer));
        }

        qstCounterTotal = qstsList.size();

        showNextQst();
        btn_confirmNext.setOnClickListener(v -> {
            if (!isAnswered) {
                if (rb0.isChecked() || rb1.isChecked() || rb2.isChecked()) {
                    checkAnswer();
                } else {
                    Toast.makeText(getActivity(), "Please Answer the Qst", Toast.LENGTH_SHORT).show();
                }
            } else {
                showNextQst();
            }
        });
    }

    private void checkAnswer() {
        isAnswered = true;
        RadioButton rbSelected = getView().findViewById(radioGroup_choices.getCheckedRadioButtonId());
        String yourAnswer = rbSelected.getText().toString();

        if (yourAnswer.equals(currentQuestion.getRightAnswer())) {
            score_phrasal++;
            tv_score.setText("Score: " + score_phrasal);
        }

        showSolution(currentQuestion.getRightAnswer());
        if (yourAnswer.equals(currentQuestion.getRightAnswer())) {
            rbSelected.setTextColor(Color.GREEN);
        } else {
            rbSelected.setTextColor(Color.RED);
        }


    }

    private void showSolution(String rightAnswer) {

        String rb0_text = rb0.getText().toString();
        String rb1_text = rb0.getText().toString();
        String rb2_text = rb0.getText().toString();

        if (rb0_text.equals(rightAnswer)) {
            rb0.setTextColor(Color.GREEN);
        } else if (rb1_text.equals(rightAnswer)) {
            rb1.setTextColor(Color.GREEN);

        } else if (rb2_text.equals(rightAnswer)) {
            rb2.setTextColor(Color.GREEN);
        }


        tv_notes.setText("The right Answer is " + currentQuestion.getRightAnswer());
        btn_confirmNext.setText("Next");
    }

    private void showNextQst() {
        rb0.setTextColor(rb_defaultColor_txt);
        rb1.setTextColor(rb_defaultColor_txt);
        rb2.setTextColor(rb_defaultColor_txt);
        radioGroup_choices.clearCheck();
        tv_notes.setText("");



        if (qst_counter < qstCounterTotal) {
            currentQuestion = qstsList.get(qst_counter);

            tv_qst.setText(currentQuestion.getTheQst());
            rb0.setText(currentQuestion.getOption0());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());

            //----set tv_theVerbFR--------
            tv_ofThePhrasal.setText(AllPhrasal.get(qst_counter).getPhrasal_fr());

            qst_counter++;

            tv_qstCount.setText("Qst " + qst_counter + "/" + qstCounterTotal);

            isAnswered = false;
            btn_confirmNext.setText("Confirm");
            if(qst_counter==qstCounterTotal) btn_confirmNext.setText("finish");
        }
        else{
            listener.setScoreClick(score_phrasal);
        }
    }

    public interface setOnChoicesFragClickListener {
        void setScoreClick(int score);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof setOnChoicesFragClickListener){
            listener = (setOnChoicesFragClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}