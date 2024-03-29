package anouar.oulhaj.p001.QuizFrags;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import anouar.oulhaj.p001.SoundManager;
import anouar.oulhaj.p001._Main.Constants;
import anouar.oulhaj.p001.CountDownTimerHelper;
import anouar.oulhaj.p001.DB.Category;
import anouar.oulhaj.p001.DB.DbAccess;
import anouar.oulhaj.p001.Question;
import anouar.oulhaj.p001.R;
import anouar.oulhaj.p001._Main.Scores;
import anouar.oulhaj.p001._Main.Utils;
import anouar.oulhaj.p001.databinding.QuizCategoriesFragmentBinding;


public class QuizCategoriesFragment extends Fragment implements CountDownTimerHelper.OnCountdownListener {
    private QuizCategoriesFragmentBinding binding;
    private QuizCategoryClickListener listener;
    private SoundManager soundManager;

    private TextToSpeech speech;
    private CountDownTimerHelper countDownTimerHelper;
    private int maxCounterTimer = 15;


    private String categoryType;

    private ColorStateList rbDefaultColorTxt;

    private List<Question> questionsList = new ArrayList<>();
    private List<Category> chosenListOfElements = new ArrayList<>();
    private int currentQstCounter, qstCounterTotal;
    private int userScore = 0;

    private boolean isAnswered;

    private Question currentQuestion;


    public QuizCategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            categoryType = bundle.getString(Constants.TAG_CATEGORY_TYPE, "VERB");
        }
    }

    public static QuizCategoriesFragment getInstance(String categoryType) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TAG_CATEGORY_TYPE, categoryType);
        QuizCategoriesFragment fragment = new QuizCategoriesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.quiz_categories_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = QuizCategoriesFragmentBinding.bind(view);
        soundManager = new SoundManager(requireActivity());

        binding.tvQuizMainQuestionCategory.setText(choosingNativeLangQuestion(Utils.nativeLanguage)); // changed



        if (categoryType.equals(Constants.IDIOM_NAME) || categoryType.equals(Constants.SENTENCE_NAME)) {
            maxCounterTimer = 30;
            RadioGroup.LayoutParams layoutParams1 = (RadioGroup.LayoutParams) binding.QuizCategoryOption1.getLayoutParams();
            layoutParams1.setMargins(0, 12, 0, 4);
            RadioGroup.LayoutParams layoutParams2 = (RadioGroup.LayoutParams) binding.QuizCategoryOption2.getLayoutParams();
            layoutParams2.setMargins(0, 12, 0, 4);
            RadioGroup.LayoutParams layoutParams3 = (RadioGroup.LayoutParams) binding.QuizCategoryOption3.getLayoutParams();
            layoutParams3.setMargins(0, 12, 0, 12);
        }
        // Initialize the countdown timer and start it if not running
        if (countDownTimerHelper == null) {
            countDownTimerHelper = new CountDownTimerHelper(maxCounterTimer * 1000L, 1000);
            countDownTimerHelper.setListener(this);
        }

        rbDefaultColorTxt = binding.QuizCategoryOption2.getTextColors();

        //------- fill Lists--------------
        DbAccess db = DbAccess.getInstance(requireActivity());
        db.open_to_read();
        ArrayList<Category> allLitOfElements = new ArrayList<>(db.getAllElementsOfCategory(categoryType, false));
        db.close();
        Collections.shuffle(allLitOfElements);
        chosenListOfElements = allLitOfElements.subList(0, Utils.maxQuestionsPerQuiz);
        qstCounterTotal = chosenListOfElements.size();
//-------------------------------------------------------------begin-----------------------------------------------------------------


        SetRandomQuestions(); // Random 3 indexes including the right index --> to choose 3 Question instance including the right Question.

        showNextQst();       // a new question set with his options + native element

        binding.btnConfirmNextCategory.setOnClickListener(v -> {   // the button Confirm/Next
            if (!isAnswered) checkAnswer();
            else {
                showNextQst();
            }
        });
        //-----------------------------------------------------------------------------------------------------------------------------------------
          txtRadioOptionToSpeech(binding.QuizCategoryOption1);
          txtRadioOptionToSpeech(binding.QuizCategoryOption2);
          txtRadioOptionToSpeech(binding.QuizCategoryOption3);

        // Initialize the TextToSpeech instance.
        speech = new TextToSpeech(requireActivity(), status -> {
            if (status == TextToSpeech.SUCCESS && !speech.isSpeaking()) {
                speech.setLanguage(Locale.ENGLISH);
            } else {
                // Handle initialization failure if needed.
                Toast.makeText(requireActivity(), "there is an error in speech", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //---------------------------------------------------------------------------------------------

    private void checkEmptyAnswerCounter() {

        listener.onShowSimpleAdsQuiz();

        isAnswered = true;
        binding.btnConfirmNextCategory.setText("Next");
      //  Random random = new Random();

       // int randomIndex = random.nextInt(Utils.phrasesIncorrectAnswers.size());
       // String text = Utils.phrasesIncorrectAnswers.get(randomIndex);
        String text = "You don't choose any answer , please make sure to choose an answer next time ";
        speakEnglish(text);
        binding.quizTvNotesCategory.setTextColor(Color.RED);
        binding.quizTvNotesCategory.setText(text);
        for (int i = 0; i < binding.quizRadioGroupCategory.getChildCount(); i++) {
            RadioButton rbMaybeCorrect = (RadioButton) binding.quizRadioGroupCategory.getChildAt(i);
            String textMaybeCorrect = rbMaybeCorrect.getText().toString();
            if (textMaybeCorrect.equals(currentQuestion.getRightAnswer())) {
                rbMaybeCorrect.setTextColor(Color.GREEN);
            } else {
                rbMaybeCorrect.setTextColor(Color.RED);
            }
        }

        // the counter is reached the final Question then change the btn to finish
        if (currentQstCounter == qstCounterTotal) {
            binding.btnConfirmNextCategory.setText("Finish");
            binding.btnConfirmNextCategory.setTextColor(Color.GREEN);
            binding.btnConfirmNextCategory.setBackgroundColor(Color.WHITE);
        }

    }
    //----------------------------------------------------------------------------------------------

    private void speakEnglish(String text) {
        if (speech != null && !text.isEmpty()) {
            speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void SetRandomQuestions() {
        for (int i = 0; i < chosenListOfElements.size(); i++) {
            List<Integer> randomList = new ArrayList<>();
            Random random = new Random();

            while (randomList.size() != 2) {
                int j = random.nextInt(chosenListOfElements.size());
                if (!randomList.contains(j) && j != i) {
                    randomList.add(j);
                }
            }

            randomList.add(i);
            Collections.shuffle(randomList);

            String mainNativeElement = choosingTheRightMainElementLang(Utils.nativeLanguage, i); // here change the Element language
            String rbOption1 = chosenListOfElements.get(randomList.get(0)).getCategoryEng();
            String rbOption2 = chosenListOfElements.get(randomList.get(1)).getCategoryEng();
            String rbOption3 = chosenListOfElements.get(randomList.get(2)).getCategoryEng();
            String rightAnswer = chosenListOfElements.get(i).getCategoryEng();

            questionsList.add(new Question(mainNativeElement, rbOption1, rbOption2, rbOption3, rightAnswer));
        }
    }


    private String choosingNativeLangQuestion(String nativeLanguage) {
        switch (nativeLanguage) {
            case Constants.LANGUAGE_NATIVE_ARABIC:
                return getResources().getString(R.string.TheQstInArabic);
            case Constants.LANGUAGE_NATIVE_SPANISH:
                return getResources().getString(R.string.TheQstInSpanish);
            default:
                return getResources().getString(R.string.TheQstInFrench);
        }

    }

    private String choosingTheRightMainElementLang(String nativeLanguage, int currentIndex) {
        switch (nativeLanguage) {
            case Constants.LANGUAGE_NATIVE_ARABIC:
                return chosenListOfElements.get(currentIndex).getCategoryAr();

            case Constants.LANGUAGE_NATIVE_SPANISH:
                return chosenListOfElements.get(currentIndex).getCategorySp();

            default:
                return chosenListOfElements.get(currentIndex).getCategoryFr();
        }
    }

    @Override
    public void onTick(int secondsUntilFinished) {
        // Toast.makeText(requireActivity(), secondsUntilFinished, Toast.LENGTH_SHORT).show();
        binding.tvCounterDownTimer.setText(String.valueOf(secondsUntilFinished));
        if(secondsUntilFinished <= 5) {
            soundManager.playSound(requireActivity(),R.raw.start_quiz);
            binding.tvCounterDownTimer.setTextColor(Color.RED);
        }
        else {
            binding.tvCounterDownTimer.setTextColor(Color.GREEN);
        }

    }

    @Override
    public void onFinish() {
        // Handle timer finished event
        counterDownTimerOver();
        int idCheckedRadio = binding.quizRadioGroupCategory.getCheckedRadioButtonId();
        if (idCheckedRadio == -1) {
            soundManager.playSound(requireActivity(),R.raw.error4);
            checkEmptyAnswerCounter();
        } else {
            checkAnswer();
        }
    }


    //-------------------------------Listener-------------------------------------------------------
    public interface QuizCategoryClickListener {
        void onShowSimpleAdsQuiz();
        void onShowVideoAdsQuiz();
        void onSetQuizCategoryResultClick(String categoryType , int pointsAdded , int elementsAdded);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof QuizCategoryClickListener) {
            listener = (QuizCategoryClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        soundManager.release();
        // Release the TextToSpeech resources when the fragment is destroyed.
        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }
        if (countDownTimerHelper != null) {
            countDownTimerHelper.stop();
        }
    }

    private void counterDownTimerOver() {

                String txt = "Time over! ";
                // Check if the TextToSpeech instance is initialized before using it.
                if (speech != null) {
                    speech.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
                }

    }


    //---------------------------gpt function-------------------------------------------
    private void showNextQst() {
        if (currentQstCounter < qstCounterTotal) {

            countDownTimerHelper.start(); //Start the timer
            resetUIForNextQuestion(); // Reset UI components for the next question
            currentQuestion = questionsList.get(currentQstCounter);
            updateUIWithQuestion(currentQuestion);
            currentQstCounter++;

            // Update the TextView with the initial time (15 seconds)
            binding.tvCounterDownTimer.setText(String.valueOf(maxCounterTimer / 1000));
        } else {
            if (countDownTimerHelper != null) countDownTimerHelper.stop();
            finishQuiz();
        }
    }

    private void resetUIForNextQuestion() {
        isAnswered = false;
        binding.btnConfirmNextCategory.setText("Confirm");   // changed R.string.confirm_text
        binding.QuizCategoryOption1.setTextColor(rbDefaultColorTxt);
        binding.QuizCategoryOption2.setTextColor(rbDefaultColorTxt);
        binding.QuizCategoryOption3.setTextColor(rbDefaultColorTxt);
        binding.quizRadioGroupCategory.clearCheck();
        binding.quizTvNotesCategory.setText("");
    }

    private void updateUIWithQuestion(Question question) {
        binding.QuizTheMainElementOfCategory.setText(question.getTheMainElement());
        binding.QuizCategoryOption1.setText(question.getOption0());
        binding.QuizCategoryOption2.setText(question.getOption1());
        binding.QuizCategoryOption3.setText(question.getOption2());
        binding.tvQuizCurrentCounterCategory.setText("Qst " + (currentQstCounter + 1) + "/" + qstCounterTotal);
    }

    private void checkAnswer() {
        int idCheckedRadio = binding.quizRadioGroupCategory.getCheckedRadioButtonId();
        if (idCheckedRadio != -1) {
            isAnswered = true;
            binding.btnConfirmNextCategory.setText("Next");    //changed R.string.next_text
            RadioButton rbSelected = getView().findViewById(idCheckedRadio);
            String yourAnswer = rbSelected.getText().toString();

            if (yourAnswer.equals(currentQuestion.getRightAnswer())) {
                handleCorrectAnswer(rbSelected);
            } else {
                handleIncorrectAnswer();
            }

            // Pause the timer when checking the answer
            countDownTimerHelper.pause();

            if (currentQstCounter == qstCounterTotal) {
                binding.btnConfirmNextCategory.setText("Finish");     // changed R.string.finish_text
                binding.btnConfirmNextCategory.setTextColor(Color.GREEN);
                binding.btnConfirmNextCategory.setBackgroundColor(Color.WHITE);
            }
        } else {
            handleNoAnswerSelected();
        }
    }

    private void handleCorrectAnswer(RadioButton rbSelected) {
        soundManager.playSound(requireActivity(),R.raw.coins1);
        userScore++;
        int randomIndex = new Random().nextInt(Utils.phrasesCorrectAnswers.size());
        String text = Utils.phrasesCorrectAnswers.get(randomIndex);
        speakEnglish(text);

        binding.tvQuizUserScoreCategory.setText("Score: " + userScore);
        binding.quizTvNotesCategory.setText(text + " : " + currentQuestion.getRightAnswer());
        rbSelected.setTextColor(Color.GREEN);  // Only set the selected radio button's text color to green
        binding.quizTvNotesCategory.setTextColor(Color.GREEN);

    }


    private void handleIncorrectAnswer() {
        soundManager.playSound(requireActivity(),R.raw.error0);
        int randomIndex = new Random().nextInt(Utils.phrasesIncorrectAnswers.size());
        String text = Utils.phrasesIncorrectAnswers.get(randomIndex);
        speakEnglish(text);

        setRadioButtonTextColor(binding.quizRadioGroupCategory, Color.RED);
        binding.quizTvNotesCategory.setTextColor(Color.RED);
        binding.quizTvNotesCategory.setText(text + " , the right answer : " + currentQuestion.getRightAnswer());

        setCorrectAnswerRadioButtonColor(binding.quizRadioGroupCategory, currentQuestion.getRightAnswer());

    }

    private void handleNoAnswerSelected() {
        isAnswered = false;
        String txt = "Please Answer the Question first";

        if (speech != null && !speech.isSpeaking()) {
            Toast.makeText(getActivity(), txt, Toast.LENGTH_SHORT).show();
            speech.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void setRadioButtonTextColor(RadioGroup radioGroup, int color) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            radioButton.setTextColor(color);
        }
    }

    private void setCorrectAnswerRadioButtonColor(RadioGroup radioGroup, String correctAnswer) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if (radioButton.getText().toString().equals(correctAnswer)) {
                radioButton.setTextColor(Color.GREEN);
                break;
            }
        }
    }

    private void finishQuiz() {
        //  here you can finish the quiz with dialog and set scores...etc        counter 6 = max 6
        isAnswered = true;
        listener.onShowVideoAdsQuiz();
        if (countDownTimerHelper != null)
            countDownTimerHelper.stop();

        finishQuizUpdateAll(userScore);
    }
    private void finishQuizUpdateAll(int userScore) {
        int pointsAdded = 0;
        int elementsAdded = 0;

        String answerCorrectCounterPerQuiz = userScore+"/"+qstCounterTotal;
        float result = userScore;

        if(result == qstCounterTotal) {
            // result 20
            pointsAdded = 5;
            elementsAdded = 3;

        } else if (result >= 0.75f * qstCounterTotal) {
            // result >=15
            pointsAdded = 3;
            elementsAdded = 2;
        } else if (result >= 0.5f * qstCounterTotal) {
            // esult >=10
            pointsAdded = 2;
            elementsAdded = 1;
        } else if(result >= 0.25f * qstCounterTotal){
            // result >= 5
            pointsAdded = 1;
            elementsAdded = 0;

        } else {
            // result <5
            pointsAdded = 0;
            elementsAdded = 0;
        }

        finishQuizUpdateEachCategory(categoryType,pointsAdded,elementsAdded);
        listener.onSetQuizCategoryResultClick(categoryType,pointsAdded,elementsAdded);

    }

    private void finishQuizUpdateEachCategory(String categoryType , int pointsAdded , int elementsAdded){
        switch(categoryType){
            case Constants.VERB_NAME:
             Scores.verbScore += pointsAdded;
             Utils.allowedVerbsNumber = getCurrentElementsNewSize(Utils.totalVerbsNumber,Utils.allowedVerbsNumber,elementsAdded);
             Scores.verbQuizCompleted++;
             Scores.verbAdded += elementsAdded;
             Scores.verbPointsAdded += pointsAdded;
             if(pointsAdded == 5) Scores.verbQuizCounterCompletedCorrectly++;
                break;
            case Constants.SENTENCE_NAME:
                Scores.sentenceScore += pointsAdded;
                Utils.allowedSentencesNumber = getCurrentElementsNewSize(Utils.totalSentencesNumber,Utils.allowedSentencesNumber,elementsAdded);
                Scores.sentenceQuizCompleted++;
                Scores.sentenceAdded += elementsAdded;
                Scores.sentencePointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.sentenceQuizCounterCompletedCorrectly++;
                break;
            case Constants.PHRASAL_NAME:
                Scores.phrasalScore += pointsAdded;
                Utils.allowedPhrasalsNumber = getCurrentElementsNewSize(Utils.totalPhrasalsNumber,Utils.allowedPhrasalsNumber,elementsAdded);
                Scores.phrasalQuizCompleted++;
                Scores.phrasalAdded += elementsAdded;
                Scores.phrasalPointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.phrasalQuizCounterCompletedCorrectly++;
                break;
            case Constants.NOUN_NAME:
                Scores.nounScore += pointsAdded;
                Utils.allowedNounsNumber = getCurrentElementsNewSize(Utils.totalNounsNumber,Utils.allowedNounsNumber,elementsAdded);
                Scores.nounQuizCompleted++;
                Scores.nounAdded += elementsAdded;
                Scores.nounPointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.nounQuizCounterCompletedCorrectly++;
                break;
            case Constants.ADJ_NAME:
                Scores.adjScore += pointsAdded;
                Utils.allowedAdjsNumber = getCurrentElementsNewSize(Utils.totalAdjsNumber,Utils.allowedAdjsNumber,elementsAdded);
                Scores.adjQuizCompleted++;
                Scores.adjAdded += elementsAdded;
                Scores.adjPointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.adjQuizCounterCompletedCorrectly++;
                break;
            case Constants.ADV_NAME:
                Scores.advScore += pointsAdded;
                Utils.allowedAdvsNumber = getCurrentElementsNewSize(Utils.totalAdvsNumber,Utils.allowedAdvsNumber,elementsAdded);
                Scores.advQuizCompleted++;
                Scores.advAdded += elementsAdded;
                Scores.advPointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.advQuizCounterCompletedCorrectly++;
                break;
            case Constants.IDIOM_NAME:
                Scores.idiomScore += pointsAdded;
                Utils.allowedIdiomsNumber = getCurrentElementsNewSize(Utils.totalIdiomsNumber,Utils.allowedIdiomsNumber,elementsAdded);
                Scores.idiomQuizCompleted++;
                Scores.idiomAdded += elementsAdded;
                Scores.idiomPointsAdded += pointsAdded;
                if(pointsAdded == 5) Scores.idiomQuizCounterCompletedCorrectly++;
                break;

        }
    }

    private void txtRadioOptionToSpeech (RadioButton radioOption) {
        radioOption.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                String text = radioOption.getText().toString();
                // Check if the TextToSpeech instance is initialized before using it.
                if (speech != null) {
                    speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    private int getCurrentElementsNewSize(int maxElementsCategory,int currentElementsCategory,int elementsAdded ){
        if((currentElementsCategory + elementsAdded) <= maxElementsCategory) {
            currentElementsCategory  += elementsAdded;
        } else{
            currentElementsCategory = maxElementsCategory;
        }
        return currentElementsCategory;


    }

}