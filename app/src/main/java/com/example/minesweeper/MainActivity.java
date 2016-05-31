package com.example.minesweeper;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final int mEMPTY_SPACE = R.drawable.empty, mINVALID_ICON_VALUE_FLAG = -99;

    //private final int BEGINNER_BOARD_SPACES = 100;
    //private final int INTERMEDIATE_BOARD_SPACES = 256;
    //private final int EXPERT_BOARD_SPACES = 400;

    private final int BEGINNER_BOARD_X = 5;//number of rows
    private final int BEGINNER_BOARD_Y = 5;//number of cols

    private final int INTERMEDIATE_BOARD_X = 10;
    private final int INTERMEDIATE_BOARD_Y = 10;

    private final int EXPERT_BOARD_X = 15;
    private final int EXPERT_BOARD_Y = 15;


    private final int BEGINNER_BOMB_PCT = 15;
    private final int INTERMEDIATE_BOMB_PCT = 16;
    private final int EXPERT_BOMB_PCT = 19;

    //private int getPreferedLevel = INTERMEDIATE_BOARD_SPACES;
    private int getPreferedLevelX = INTERMEDIATE_BOARD_X;
    private int getPreferedLevelY = INTERMEDIATE_BOARD_Y;
    private int getPreferedPCT = INTERMEDIATE_BOMB_PCT;

    private final double BEGINNER_SCALE_P = 13.5;
    private final double INTERMEDIATE_SCALE_P = 22;
    private final double EXPERT_SCALE_P = 28;

    private final double BEGINNER_SCALE_L = .5;
    private final double INTERMEDIATE_SCALE_L = 20;
    private final double EXPERT_SCALE_L = 6;



    private double getPreferedScale = INTERMEDIATE_SCALE_P;

    //private final int

    private boolean mBombDetonated;
    //private int totalBombs = INTERMEDIATE_BOMB_COUNT;

    private boolean mPrefUseAutoSave, mGameOver;
    private int mCurrentPosition, mPriorPosition = mINVALID_ICON_VALUE_FLAG;

    private String mKEY_USE_AUTO_SAVE;

    private final String mKEY_BOARD = "BOARD";
    private final String mKEY_GAME_OVER = "GAME_OVER";
    private final String mPREFS = "PREFS";



    private CardViewImageAdapter mAdapter;

    private TextView mStatusBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Snackbar mSbGame;
    private View mSbParentView;
    private final String mKEY_BOARD_CHARS = "CHARS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initializePreferences();

        setContentView(R.layout.activity_main);

        initGUI();

        Boolean dataFromAutoSave=false;
        if(savedInstanceState==null){
            Bundle prefData=restoreAllDataFromPrefs();
            if(prefData!=null && mPrefUseAutoSave && prefData.containsKey("boolean>mines")){//last one is a test for a sample of data in prefs that would be there if was autosaved
                savedInstanceState=prefData;
                dataFromAutoSave=true;
            }}
        if (savedInstanceState!=null){
            getPreferedLevelX = savedInstanceState.getInt("rows");
            getPreferedLevelY = savedInstanceState.getInt("cols");
            getPreferedScale = savedInstanceState.getDouble("SCALEP");
        }

        createUnfilledBoard();

        initializeSnackBar();



        // If we are starting a fresh Activity (meaning, not after rotation), then do initial setup
        if (savedInstanceState == null) {
            setupInitialSession();

        }
        // If we're in the middle of a game then onRestoreInstanceState will restore the App's state
        if(dataFromAutoSave){
            onRestoreInstanceState(savedInstanceState);
            //mAdapter.restoreSerializedData(savedInstanceState);
        }
    }

    private void initializePreferences() {
        mKEY_USE_AUTO_SAVE = getString(R.string.key_use_auto_save);
    }

    private void initGUI() {
        initializeStatusItems();
        initializeSwipeRefreshLayout();
    }

    /**
     * Initializes the members that are relevant to only portrait or landscape
     */
    private void initializeStatusItems() {
        mStatusBar = (TextView) findViewById(R.id.textViewStatusBar);
    }

    private void initializeSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                prepareForNewGame();
                startNewOrResumeGameState();
            }
        });
    }

    private void prepareForNewGame() {
        mGameOver=false;//maybe this will work...
        createUnfilledBoard();
        resetCurrentAndPriorPositions();
        dismissSnackBarIfShown();
    }

    private void resetCurrentAndPriorPositions() {
        mPriorPosition = mINVALID_ICON_VALUE_FLAG;
        mCurrentPosition = mINVALID_ICON_VALUE_FLAG;
    }

    /**
     * Called as the last step in the new game process and when starting app.
     */
    private void startNewOrResumeGameState() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void createUnfilledBoard() {

        // Create the adapter for later use in the RecyclerView
        mAdapter = new CardViewImageAdapter(getApplicationContext(), getPreferedLevelX, getPreferedLevelY,getPreferedPCT,R.drawable.empty, getPreferedScale,this);

        // set the listener which will listen to the clicks in the RecyclerView
        //mAdapter.setOnItemClickAndLongClickListener(listener);


        // get a reference to the RecyclerView
        RecyclerView board = (RecyclerView) findViewById(R.id.grid_board);
        assert board != null;

        // get a reference to a new LayoutManager for the RecyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, getPreferedLevelY);//this is where we should tell it how many columsn we have
        layoutManager.setAutoMeasureEnabled(true);


        // set the adapter as the data source (model) for the RecyclerView
        board.setHasFixedSize(false);
//        board.addItemDecoration(new SpacesItemDecoration(0));
        board.setLayoutManager(layoutManager);
        board.setAdapter(mAdapter);
    }

    private void initializeSnackBar() {
        // Initialize (but do not show) SnackBar
        mSbParentView = findViewById(R.id.cl_main);
        assert mSbParentView != null;

        mSbGame = Snackbar.make(mSbParentView, "Test", Snackbar.LENGTH_INDEFINITE);
    }

    /**
     * Called from onCreate() only if the user just entered the app from the home screen
     * as opposed to when destroying/recreating from e.g. an orientation change
     * <p/>
     * Steps:
     * 1. In case this is the first run ever, set the default values in shared prefs
     * 2. Then, set the last Game results message to first game of session
     * Next:
     * 3. Prepare for new game (but no initial computer turn)
     * 4. Restore all data from Shared Preferences
     * 5. Start or resume new game, meaning turn off animation
     * and conditionally take initial/next computer turn
     */
    private void setupInitialSession() {
        setDefaultValuesForPreferences();
        prepareForNewGame();
        //restoreAllDataFromPrefs(); now at the top of oncreate
        startNewOrResumeGameState();
    }

    private Bundle restoreAllDataFromPrefs() {
        restoreGameTypeAndAutoSaveStatus();
        return restoreLastStateIfAutoSaveIsOn();
    }

    /**
     * This method essentially takes what was saved above and restores it.
     */
    private void restoreGameTypeAndAutoSaveStatus() {
        // Since this is for reading only, no editor is needed unlike in onSaveRestoreState
        SharedPreferences preferences = getSharedPreferences(mPREFS, MODE_PRIVATE);

        // restore AutoSave preference value
        mPrefUseAutoSave = preferences.getBoolean(mKEY_USE_AUTO_SAVE, true);
        System.out.println("autosave = " + mPrefUseAutoSave);
    }

    private Bundle restoreLastStateIfAutoSaveIsOn() {
        SharedPreferences preferences = getSharedPreferences(mPREFS, MODE_PRIVATE);

         if (mPrefUseAutoSave) {

        // restore "game-over" state
        mGameOver = preferences.getBoolean(mKEY_GAME_OVER, false);
        //if (!mGameOver){dismissSnackBarIfShown();}// if it works: a faster way to fix the bug I'm seeing}

        return restoreAllBoardData(preferences);
        }
        return null;//if not autosave
    }

    private Bundle restoreAllBoardData(SharedPreferences preferences) {
        // restore the board icon values from SharedPreferences
        //restoreBoardIcons(preferences);

        // restore the board from SharedPreferences
        //restoreBoard(preferences);

        return loadPreferencesBundle(preferences,"bundle");



    }

    /**
     * This method's Superclass implementation saves to its "Bundle" argument the values of Views
     * We will add to that bundle the variables that we need to persist across destroy/create cycles
     *
     * @param outState bundle containing the saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save contents of views, etc. automatically
        super.onSaveInstanceState(outState);

        // save the "game over" state
        outState.putBoolean(mKEY_GAME_OVER, mGameOver);

        // save the current autoSave boolean
        outState.putBoolean(mKEY_USE_AUTO_SAVE, mPrefUseAutoSave);

        outState.putDouble("SCALEP",getPreferedScale);
        /*
        // save the board layout as a whole to the bundle to be saved
        outState.putIntArray(mKEY_BOARD, mAdapter.getDataOfModel());
        outState.putCharArray(mKEY_BOARD_CHARS, mAdapter.getSecondaryDataOfModel());
        */

        outState.putAll(mAdapter.getCardViewDataSerialized());
    }

    /**
     * This method's Superclass implementation restore the values of Views, as above.
     * We will add to that the functionality to have it restore our variables that we saved above.
     *
     * @param savedInstanceState the bundle containing the saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore contents of views, etc. automatically
        super.onRestoreInstanceState(savedInstanceState);

        // restore autoSave
        mPrefUseAutoSave = savedInstanceState.getBoolean(mKEY_USE_AUTO_SAVE);

        // restore game over
        mGameOver = savedInstanceState.getBoolean(mKEY_GAME_OVER);
        System.out.println(mGameOver);

        // have mAdapter restore its old state
        mAdapter.restoreSerializedData(savedInstanceState);

        // show game over message if the current saved game had already ended
        showGameOverSnackBarIfGameOver();
    }



    private void showGameOverSnackBarIfGameOver() {
        if (mGameOver) {
            showGameOverSB(false);
        }
    }

    /**
     * In addition to the super-class's onPause, save the board to shared prefs now, just in case...
     */
    @Override
    protected void onPause() {
        super.onPause();
        savePrefAndBoardToSharedPref();
    }

    private void savePrefAndBoardToSharedPref() {
        // Create a SP object that (creates if needed and) uses the value of mPREFS as the file name
        SharedPreferences preferences = getSharedPreferences(mPREFS, MODE_PRIVATE);

        // Create an Editor object to write changes to the preferences object above
        SharedPreferences.Editor editor = preferences.edit();

        // clear whatever was set last time
        editor.clear();

        // save autoSave preference
        editor.putBoolean(mKEY_USE_AUTO_SAVE, mPrefUseAutoSave);
        System.out.println("spabtsp autosave = " + mPrefUseAutoSave);


        // if autoSave is on then save the board
        saveBoardToSharedPrefsIfAutoSaveIsOn(editor);

        // apply the changes to the XML file in the device's storage
        editor.apply();

    }

    private void saveBoardToSharedPrefsIfAutoSaveIsOn(SharedPreferences.Editor editor) {
        // (Only) if autoSave is enabled, then save the board and current player to the SP file
        System.out.println("MainActivity.saveBoardToSharedPrefsIfAutoSaveIsOn");
        System.out.println("autosave = " + mPrefUseAutoSave);
        //System.out.println("");
        if (mPrefUseAutoSave) {

            // save "game over" state
            editor.putBoolean(mKEY_GAME_OVER, mGameOver);

            saveAllBoardData(editor);
        }
    }

    private void saveAllBoardData(SharedPreferences.Editor editor) {


        Bundle bundle = new Bundle();
        onSaveInstanceState(bundle);
        savePreferencesBundle(editor,"bundle",bundle);
        editor.commit();
        //Bundle mbundle= mAdapter.getCardViewDataSerialized();
        //savePreferencesBundle(editor,"bundle2",mbundle);
        // save the board icon IDs to SharedPreferences
        //saveBoardIcons(editor);

        // save the board to SharedPreferences
        //saveBoard(editor);

        // save the tints to SharedPreferences (if gameOver)
//        saveTints(editor);
    }


    /**
     * Stores the current IDs of the X, O and XO icons.
     * This is needed for auto-save purposes in case the IDs change between runs
     * such as if a new build is released
     *
     * @param editor The SharedPreferences Editor that saves the icons
     */
    private void saveBoardIcons(SharedPreferences.Editor editor) {
        // TODO save state by saving numbers
    }

    private void saveBoard(SharedPreferences.Editor editor) {
        String currentKeyName;// save board one square at a time
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            currentKeyName = mKEY_BOARD + i;
            editor.putLong(currentKeyName, mAdapter.getItemId(i));
        }
    }

    private void setDefaultValuesForPreferences() {
        PreferenceManager.setDefaultValues(getApplicationContext(),
                R.xml.prefs_that_need_defaults, true);
    }

    private void restoreBoardIcons(SharedPreferences preferences) {
        // TODO restore old numbers onto board
//        mOLD_ICON_X = (int) preferences.getLong(mKEY_ICON_X, mEMPTY_SPACE);
//        mOLD_ICON_O = (int) preferences.getLong(mKEY_ICON_O, mEMPTY_SPACE);
//        mOLD_ICON_XO = (int) preferences.getLong(mKEY_ICON_XO, mEMPTY_SPACE);
    }

    private void restoreBoard(SharedPreferences preferences) {
        throw new RuntimeException("shouldnt be using right now");
//        String currentKeyName;
//        int currentSpace;
//
//        // restore the board one square at a time
//        for (int i = 0; i < mAdapter.getItemCount(); i++) {
//            currentKeyName = mKEY_BOARD + i;
//            currentSpace = (int) preferences.getLong(currentKeyName, mEMPTY_SPACE);
//            currentSpace = getValidCurrentSpace(currentSpace);
//            mAdapter.setImage(i, currentSpace);
//        }
    }

    private int getValidCurrentSpace(int currentSpace) {
        // The XO must be element #0 because the default pref value is empty space
        // So #0 will always match if the app is being run for the first time
        final int[] CURRENT_ICONS = {R.drawable.empty};

        int validIcon = mINVALID_ICON_VALUE_FLAG;

        for (int i = 0; i < CURRENT_ICONS.length && validIcon == mINVALID_ICON_VALUE_FLAG; i++) {
            validIcon = (currentSpace ==
                    //Put current number of current space
                    1 || currentSpace == CURRENT_ICONS[i])
                    ? CURRENT_ICONS[i] : validIcon;
        }

        return validIcon != mINVALID_ICON_VALUE_FLAG ? validIcon : R.drawable.empty;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // prepares the value for the mPrefUseAutoSave checked item in the menu
        // meaning: check or remove the check in the menu to match the user value for this pref.
        menu.findItem(R.id.action_autoSave).setChecked(mPrefUseAutoSave);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_newGame: {
                startNewGameIncludingSRAnimation();
                return true;
            }

            case R.id.action_newBeginnerGame: {
                createNewGame(BEGINNER_BOARD_X,BEGINNER_BOARD_Y, BEGINNER_SCALE_P, BEGINNER_SCALE_L, BEGINNER_BOMB_PCT);//still needs to be adapted
                return true;
            }

            case R.id.action_newIntermediateGame: {
                createNewGame(INTERMEDIATE_BOARD_X,INTERMEDIATE_BOARD_Y, INTERMEDIATE_SCALE_P, INTERMEDIATE_SCALE_L, INTERMEDIATE_BOMB_PCT);
                return true;
            }

            case R.id.action_newExpertGame: {
                createNewGame(EXPERT_BOARD_X,EXPERT_BOARD_Y, EXPERT_SCALE_P, EXPERT_SCALE_L, EXPERT_BOMB_PCT);
                return true;
            }

            case R.id.action_autoSave: {
                toggleItemCheck(item);
                mPrefUseAutoSave = item.isChecked();
                return true;
            }
            case R.id.action_about: {
                showTTTDialog(getString(R.string.aboutDialogTitle),
                        getString(R.string.aboutDialog_banner)
                );
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNewGame(int boardX, int boardY, double scaleP, double scaleL, int bombPCT) {//still needs to be adapted
        getPreferedLevelX = boardX;
        getPreferedLevelY = boardY;
        getPreferedScale = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT
                ? scaleP
                : scaleL;
        Log.d("SCALING", "The dimens are " + getPreferedLevelX + ","+ getPreferedLevelY);
        Log.d("SCALING", "The orientation is " + ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //totalBombs = bombPCT;
        startNewGameIncludingSRAnimation();
    }

    private void toggleItemCheck(MenuItem item) {
        item.setChecked(!item.isChecked());
    }

    // ---------------------------------------------------------------------------------------------
    // This private anonymous inner-class listener is registered to the RecyclerView.
    // ---------------------------------------------------------------------------------------------

//    private final CardViewImageAdapter.OIClickAndLongClickListener
//            listener = new CardViewImageAdapter.OIClickAndLongClickListener() {
//
//        @Override
//        public void onItemLongClick(int position, View v) {
//            // if the game is already over then there is nothing more to do here
//            if (mGameOver) {
//                showGameOverSB(true);
//            } else {
//                /*ToggleButton t = (ToggleButton) v;
//                Log.d("TOGGLE", t.getTextOff().toString());
//
//                // If someone long clicks: a flag should appear and bomb count should go down 1.
//                if (t.isClickable()) {
//                    t.setClickable(false);
//                    t.setText("F");
//                    totalBombs--;
//                }
//                // If someone long clicks a flag: flag should disappear and bomb count should go up 1.
//                else {
//                    t.setClickable(true);
//                    t.setText("");
//                    totalBombs++;
//                }*/
//                mAdapter.setChar(position, 'F');
//
//            }
//            Log.d("LONG_CLICK", "Long Click");
//        }
//
//        public void onItemClick(int position, View v) {
//            // if the game is already over then there is nothing more to do here
//            if (mGameOver) {
//                showGameOverSB(true);
//            } else {
//               /* ToggleButton t = (ToggleButton) v;
//                Log.d("TOGGLE", t.getTextOff().toString());
//                t.setEnabled(false);
//
//                // if bomb then set drawable to bomb
//                // This sets clicked button to bomb
//                t.setBackgroundDrawable(getResources().getDrawable(R.drawable.minesweeper_bomb))*/
//
//                Random random = new Random();
//                int number = random.nextInt(6);
//
//                if (number % 2 == 0) {
//                    mAdapter.setImage(position, R.drawable.minesweeper_bomb);
//                } else {
//                    mAdapter.setChar(position, (char)(number + '1'));
//                    Log.d("CLICK", "Number should be char value of "
//                            + (number + 1) + " which is " + (char)(number + '1'));
//                }
//                //else set text to whatever number should be
//                //t.setText("1");
//
//            }
//            Log.d("LONG_CLICK", "Click");
//        }
//    };

    private void startNewGameIncludingSRAnimation() {
        // start animation
        mSwipeRefreshLayout.setRefreshing(true);
        prepareForNewGame();
        startNewOrResumeGameState();
    }

    public void doGameOverTasks() {
        mGameOver = true;
    }



    public void showGameOverSB(boolean gameAlreadyOver) {
        StringBuilder sbText = generateGameOverMessage(gameAlreadyOver);
        mSbGame = Snackbar.make(mSbParentView, sbText, Snackbar.LENGTH_INDEFINITE);
        mSbGame.setAction(R.string.action_newGame, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGameIncludingSRAnimation();
            }
        })
                .show();
    }

    @NonNull
    private StringBuilder generateGameOverMessage(boolean gameAlreadyOver) {
        StringBuilder sbText = new StringBuilder(getString(R.string.info_game_over));
        sbText.append(' ');

        if (gameAlreadyOver) {
            sbText.append("Game already over");
        }
        return sbText;
    }

    private boolean isGameOver() {
        // Assume Game is over
        boolean gameOver = true;

        if (mBombDetonated) {
            showGameOverSB(gameOver);
        } else {
            gameOver = false;
        }
        return gameOver;
    }

    private void dismissSnackBarIfShown() {
        if (mSbGame.isShown()) {
            mSbGame.dismiss();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // This method is called from showAbout (MenuItem item), specified in the item's onClick in XML.
    // It is also called from the Game Over code sequence
    //
    // Note: To use the new "Material Design"-look AlertDialog, the AlertDialog's import should be:
    // import android.support.v7.app.AlertDialog;
    // ---------------------------------------------------------------------------------------------
    private void showTTTDialog(String title, String message) {
        // Create listener for use with dialog window (could also be created anonymously below...
        DialogInterface.OnClickListener dialogOnClickListener =
                createTTTOnClickListener();

        // Create dialog window
        AlertDialog TTTAlertDialog = initDialog(title, message, dialogOnClickListener);

        // Show the dialog window
        TTTAlertDialog.show();

    }

    private AlertDialog initDialog(String title, String message,
                                   DialogInterface.OnClickListener dialogOnClickListener) {
        AlertDialog TTTAlertDialog;
        TTTAlertDialog = new AlertDialog.Builder(MainActivity.this).create();
        TTTAlertDialog.setTitle(title);
        TTTAlertDialog.setIcon(R.mipmap.ic_launcher);
        TTTAlertDialog.setMessage(message);
        TTTAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.OK), dialogOnClickListener);
        return TTTAlertDialog;
    }

    private DialogInterface.OnClickListener createTTTOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        };
    }


    // ---------------------------------------------------------------------------------------------
    // LG work-around to prevent crash when user hits menu button
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo("LGE") == 0)) {
            Log.i("LG", "LG Legacy Device Detected");
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo("LGE") == 0)) {
            openOptionsMenu();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }



    private static final String SAVED_PREFS_BUNDLE_KEY_SEPARATOR = "§§";

    /**
     * Save a Bundle object to SharedPreferences.
     *
     * NOTE: The editor must be writable, and this function does not commit.
     *
     * @param editor SharedPreferences Editor
     * @param key SharedPreferences key under which to store the bundle data. Note this key must
     *            not contain '§§' as it's used as a delimiter
     * @param preferences Bundled preferences
     */
    public static void savePreferencesBundle(SharedPreferences.Editor editor, String key, Bundle preferences) {
        Set<String> keySet = preferences.keySet();
        Iterator<String> it = keySet.iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;

        while (it.hasNext()){
            String bundleKey = it.next();
            Object o = preferences.get(bundleKey);
            if (o == null){
                editor.remove(prefKeyPrefix + bundleKey);
            } else if (o instanceof Integer){
                editor.putInt(prefKeyPrefix + bundleKey, (Integer) o);
            } else if (o instanceof Long){
                editor.putLong(prefKeyPrefix + bundleKey, (Long) o);
            } else if (o instanceof Double){
                editor.putLong(prefKeyPrefix + bundleKey, ((Double) o).longValue());
            } else if (o instanceof Boolean){
                editor.putBoolean(prefKeyPrefix + bundleKey, (Boolean) o);
            } else if (o instanceof CharSequence){
                editor.putString(prefKeyPrefix + bundleKey, ((CharSequence) o).toString());
            } else if (o instanceof Bundle){
                savePreferencesBundle(editor, prefKeyPrefix + bundleKey, ((Bundle) o));
            } else if (o instanceof Object[] || o.getClass().isArray()){
                Gson gson = new Gson();
                String string = gson.toJson(o);
                editor.putString(prefKeyPrefix + bundleKey,string);
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                    try {
//                        JSONArray json = new JSONArray(o);
//                        String string = json.toString();
//                        editor.putString(prefKeyPrefix + bundleKey,string);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }
    }

    private static int[] toArray(String json, Gson parser) {
        return parser.fromJson(json, int[].class);
    }
    /**
     * Load a Bundle object from SharedPreferences.
     * (that was previously stored using savePreferencesBundle())
     *
     * NOTE: The editor must be writable, and this function does not commit.
     *
     * @param sharedPreferences SharedPreferences
     * @param key SharedPreferences key under which to store the bundle data. Note this key must
     *            not contain '§§' as it's used as a delimiter
     *
     * @return bundle loaded from SharedPreferences
     */
    public static Bundle loadPreferencesBundle(SharedPreferences sharedPreferences, String key) {
        Bundle bundle = new Bundle();
        Map<String, ?> all = sharedPreferences.getAll();
        Iterator<String> it = all.keySet().iterator();
        String prefKeyPrefix = key + SAVED_PREFS_BUNDLE_KEY_SEPARATOR;
        Set<String> subBundleKeys = new HashSet<String>();
        boolean wasDataThere = false;
        while (it.hasNext()) {
            wasDataThere=true;
            String prefKey = it.next();

            if (prefKey.startsWith(prefKeyPrefix)) {
                String bundleKey = StringUtils.removeStart(prefKey, prefKeyPrefix);

                if (!bundleKey.contains(SAVED_PREFS_BUNDLE_KEY_SEPARATOR)) {

                    Object o = all.get(prefKey);
                    if (o == null) {
                        // Ignore null keys
                    } else if (o instanceof Integer) {
                        bundle.putInt(bundleKey, (Integer) o);
                    } else if (o instanceof Long) {
                        bundle.putLong(bundleKey, (Long) o);
                    } else if (o instanceof Boolean) {
                        bundle.putBoolean(bundleKey, (Boolean) o);
                    } else if (o instanceof CharSequence) {

                        Gson gson = new Gson();
                        Type type = null;

                        if (bundleKey.startsWith("int>")){
                            type = new TypeToken<int[][]>() {}.getType();}
                        else if (bundleKey.startsWith("boolean>")){
                            type = new TypeToken<boolean[][]>() {}.getType();}
                        else if (bundleKey.startsWith("char>")){
                            type = new TypeToken<char[][]>() {}.getType();}
//                        else if (bundleKey.startsWith("string>")){
//                            type = new TypeToken<ArrayList<String>>() {}.getType();}*/
//                        else if (bundleKey.contains(">")) {
//                            type = new TypeToken<Object[][]>() {}.getType();
//                        }

                        if (type != null){//if was an array
                            Object serializable = gson.fromJson((String) o, type);
                            bundle.putSerializable(bundleKey, (Serializable) serializable);}
                        else{//actually a string
                            bundle.putString(bundleKey, ((CharSequence) o).toString());
                        }
                    }
                }
                else {
                    // Key is for a sub bundle
                    String subBundleKey = StringUtils.substringBefore(bundleKey, SAVED_PREFS_BUNDLE_KEY_SEPARATOR);
                    subBundleKeys.add(subBundleKey);
                }
            }
            else {
                // Key is not related to this bundle.
            }
        }

        // Recursively process the sub-bundles
        for (String subBundleKey : subBundleKeys) {
            Bundle subBundle = loadPreferencesBundle(sharedPreferences, prefKeyPrefix + subBundleKey);
            bundle.putBundle(subBundleKey, subBundle);
        }


        if (wasDataThere) return bundle;
        else return null;
    }

}