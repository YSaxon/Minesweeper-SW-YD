package com.example.minesweeper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

public class CardViewImageAdapter extends RecyclerView.Adapter<CardViewImageAdapter.ViewHolder>
{
    private static final int HARDCODED_HEIGHT = 100;
    //private static OIClickAndLongClickListener sOIClickAndLongClickListener;
    private clickHandler sClickHandler = new clickHandler();


    private final Context mContext;

    private final Random rand = new Random();

    private int rows;
    private int cols;

    private boolean[][] mIsMine;
    private boolean[][] mIsRevealed;
    private boolean[][] mIsFlagged;
    private int[][] mMinesSurrounding;

    private char[][] mChars;
    private int[][] mImages;

    private final double mScaleHeightP, mScaleHeightL = 20;
    private final int mINVALID_FLAG = -99;

    private final boolean longClickOnRevealedRevealsNeighbors = true;
    private final boolean gameOverOnMineExplode = true;


    public CardViewImageAdapter (Context context, int rows,int cols, int minePercentage, int defaultDrawableID, double scaleVertical)
    {
        this.rows = rows;
        this.cols = cols;

        mContext = context;

        mScaleHeightP = scaleVertical;

        mIsMine= new boolean[rows][cols];
        mIsRevealed=new boolean[rows][cols];
        mIsFlagged=new boolean[rows][cols];

        mMinesSurrounding = new int[rows][cols];

        mImages = new int[rows][cols];
        mChars = new char[rows][cols];

        generateMines(minePercentage);
        calcNumSurrounding();

        fillMemberArrays(defaultDrawableID);


    }

    private void calcNumSurrounding() {

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {//for each square
                int sumOfSurround = 0;
                ArrayList<int[]> neighbors=getNeighbors(x,y);
                for(int[] pair:neighbors){
                    if (mIsMine[pair[0]][pair[1]]){sumOfSurround++;}
                }
                mMinesSurrounding[x][y]=sumOfSurround;
            }
        }
    }


    private void generateMines(int minePercentage) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
               mIsMine[i][j]=rand.nextInt(100)<=minePercentage;
            }
        }

    }

    private void fillMemberArrays (int defaultDrawableID)
    {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mImages[i][j] = defaultDrawableID;
                mChars[i][j] = ' ';//Character.highSurrogate(mMinesSurrounding[i][j]);//' '
            }

        }
    }

    /*public void setOnItemClickAndLongClickListener(OIClickAndLongClickListener oiClickAndLongClickListener)
    {
        CardViewImageAdapter.sOIClickAndLongClickListener = oiClickAndLongClickListener;
    }*/



    public void setChar(int x,int y, char c) {
        mChars[x][y] = c;       //Character.forDigit(mMinesSurrounding[x][y],10);
        notifyDataSetChanged();
    }

    public void setImage (int x,int y, int newDrawableID)
    {
        mImages[x][y] = newDrawableID;
        // Update view to reflect updates to model
        notifyDataSetChanged();
    }

    @Override
    public CardViewImageAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        // Inflate a new layout that consists of what is contained in the RV Item XML file
        View itemLayoutView = LayoutInflater.from (parent.getContext ())
                .inflate (R.layout.rv_card_image_item, parent, false);

        // Create a new ViewHolder with that newly-inflated View
        CardViewImageAdapter.ViewHolder viewHolder = new ViewHolder (itemLayoutView);
        adjustScaling (viewHolder);

        // return the created and then modified ViewHolder
        return viewHolder;
    }

    private void adjustScaling (ViewHolder viewHolder)
    {
        // Scale that ImageView's height to match a portion of the actual screen size...

        // Get a reference to the ImageView inside this newly-inflated View
        Button buttonInNewlyInflatedView = viewHolder.mCurrentButton;

        // Get a reference to the already existing LayoutParameters
        ViewGroup.LayoutParams currentLayoutParams = buttonInNewlyInflatedView.getLayoutParams ();

        // Change the height to match the appropriate size for this screen's current actual height
        currentLayoutParams.height = HARDCODED_HEIGHT; //getHeightSize ();

        // Set the LP of this ImageView to point to that newly-adjusted LP with adjusted height
        buttonInNewlyInflatedView.setLayoutParams (currentLayoutParams);
    }

    @Override public void onBindViewHolder (CardViewImageAdapter.ViewHolder holder, int position)
    {
        Button currentButton = holder.mCurrentButton;
        Drawable currentImage = ContextCompat.getDrawable(mContext,mImages[xFromPos(position)][yFromPos(position)]);
        char currentChar = mChars[xFromPos(position)][yFromPos(position)];

        currentButton.setBackgroundDrawable (currentImage);
        currentButton.setText(String.valueOf(currentChar));
        //currentButton.setLongClickable(true);
    }

    private int yFromPos(int position) {
        return position%cols;
    }

    private int xFromPos(int position) {
        return position/cols;
    }


    private int getHeightSize ()
    {
        // constants - try changing these values to see the effect on image-spacing in the GridView
        final double SCALE = mScaleHeightP, SCALE_LANDSCAPE = mScaleHeightL;
        final int HEIGHT_PARAMETER;

        // getResources() is access via the Context passed in to the constructor - for orientation
        Resources resources = mContext.getResources ();

        // The following two items are methods in the resources object reference above

        // Create a reference to a DisplayMetrics object so we can get the current resolution
        DisplayMetrics displayMetrics = resources.getDisplayMetrics ();

        // Create a reference to a Configuration object so we can get the screen orientation
        Configuration configuration = resources.getConfiguration ();

        // Using the reference variables created above, determine if orientation is landscape
        boolean isLandscape = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE);

        // set the scaling numbers
        double scaleVertical = isLandscape ? SCALE_LANDSCAPE : SCALE;

        // store the screen width and height using these scaling numbers and the screen's pixel size
        double screenHeight = displayMetrics.heightPixels;

        // create the values for LayoutParameter
        HEIGHT_PARAMETER = (int) (screenHeight / scaleVertical);
        return HEIGHT_PARAMETER;
    }

    @Override public int getItemCount ()
    {
        return rows*cols;
    }

    @Override
    public long getItemId (int position)
    {
        throw new NoSuchElementException("I don't think this method is in use anymore");
        //return 0;
        //return position >= 0 && position < mImages.length ? mImages[xFromPos(position)][yFromPos(position)] : -1;
    }

    public int[][] getDataOfModel ()
    {
        return mImages.clone ();
    }

    public char[][] getSecondaryDataOfModel() {
        return mChars.clone();
    }

    private void handleClick() {
    }

/*    public int [] getSecondaryDataOfModel()
    {
        return mImageTints.clone();
    }*/

    // Inner Class - references a RecyclerView View item
    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        // must be public and final so that it is accessible in the outer class
        final Button mCurrentButton;

        // The constructor calls super and creates a public reference to this ViewHolder's ImageView
        // sets this current class to handle any clicks, which passes that to the calling Activity
        // if that calling activity implements OIClickAndLongClickListener, which it should
        public ViewHolder (View itemLayoutView)
        {
            super (itemLayoutView);
            mCurrentButton = (Button) itemLayoutView.findViewById (R.id.rv_image_item);

            mCurrentButton.setOnClickListener(this);
            mCurrentButton.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            //need to figure out how to get array position out of this, probably with some maths involving linear position
            //then can basically copy logic from other side
            int position = getAdapterPosition();
            sClickHandler.onItemLongClick(position,v);
            return true;
        }

        @Override
        public void onClick (View v)
        {

            //sOIClickAndLongClickListener.onItemClick (getAdapterPosition (), v);
            int position = getAdapterPosition();
            sClickHandler.onItemClick(position,v);
            notifyItemChanged(position);





            //if (!isRevealed&&!isFlagged){reveal();}
        }


    }

/*    // used to send data out of Adapter - implemented in the calling Activity/Fragment
    @SuppressWarnings ("UnusedParameters")
    public interface OIClickAndLongClickListener
    {
        void onItemLongClick (int position, View v);
        void onItemClick (int position, View v);
    }
*/

    public class clickHandler
    {
        void onItemClick (int position, View v){
            int x=xFromPos(position);
            int y=yFromPos(position);
            if (!mIsRevealed[x][y]&&!mIsFlagged[x][y]){reveal(x,y);}
    }

        void onItemLongClick (int position, View v){
            int x=xFromPos(position);
            int y=yFromPos(position);
            if (!mIsRevealed[x][y]==true){toggleFlag(x,y);}
            else if (longClickOnRevealedRevealsNeighbors) {revealNeighbors(x,y);}//possibly make a setting to control this
        }

    }

    private void toggleFlag(int x, int y) {
        mIsFlagged[x][y]=!mIsFlagged[x][y];
        //setChar(x,y,mIsFlagged[x][y]?'F':' ');
        //mImages[x][y]=R.drawable.minesweeper_bomb;
        setImage(x,y,mIsFlagged[x][y]?R.drawable.flagicon:R.drawable.empty);

    }

    private void reveal(int x, int y) {
        if(!mIsRevealed[x][y]&&!mIsFlagged[x][y]){
        System.out.println("reveal called for: "+x+","+y+"  >  "+mMinesSurrounding[x][y]+" around    ;  revealed "+mIsRevealed[x][y]);
        mIsRevealed[x][y]=true;
        if(mIsMine[x][y]==true){explode(x,y);}
        else setChar(x,y, Integer.toString(mMinesSurrounding[x][y]).charAt(0));

        if (mMinesSurrounding[x][y]==0){
            System.out.println("reveal neighbors called");
            revealNeighbors(x,y);
        }}
    }

    private void revealNeighbors(int x, int y) {
        ArrayList<int[]> neighbors=getNeighbors(x,y);
       for(int[] pair:neighbors)
            if (!(mIsRevealed[pair[0]][pair[1]])==true){ reveal(pair[0],pair[1]);};
        }



    public ArrayList<int[]> getNeighbors(int x, int y) {
        ArrayList<int[]> neighborList = new ArrayList<int[]>();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {//check neighbors (+1,0,-1 on both axes)
                if (!(a == 0 && b == 0 || x + a < 0 || x + a >= rows || y + b < 0 || y + b >= cols)) {//if a valid neighbor
                    int[] pair = new int[2];
                    pair[0]=x+a;
                    pair[1]=y+b;
                    neighborList.add(pair);}}}
        return  neighborList;

    }

    private void explode(int x, int y) {
        setImage(x,y,R.drawable.minesweeper_bomb);//R.drawable.minesweeper_bomb;
        notifyDataSetChanged();
        if(gameOverOnMineExplode){failGame();}

    }

    private void failGame() {
        showAllBombs();
        disableClicks();

    }

    private void disableClicks() {
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {//for each square
                {
                    mIsRevealed[x][y]=true;//a little hacky but it should work
                }

            }}
    }

    private void showAllBombs() {
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {//for each square
                if (mIsMine[x][y]&&!mIsRevealed[x][y]) {
                    setImage(x, y, R.drawable.minesweeper_bomb);
                }

            }}
    }


    public Bundle getCardViewDataSerialized(){
        Bundle allInOne = new Bundle();
        //the important stuff
        allInOne.putInt("rows",rows);
        allInOne.putInt("cols",cols);

        allInOne.putSerializable("boolean>mines",mIsMine);
        allInOne.putSerializable("boolean>flags",mIsFlagged);
        allInOne.putSerializable("boolean>revealed",mIsRevealed);

        //less important stuff that could just be regenerated
        allInOne.putSerializable("int>numSurround",mMinesSurrounding);
        allInOne.putSerializable("int>images",mImages);
        allInOne.putSerializable("char>chars",mChars);
        return allInOne;
    }
    public void restoreSerializedData(Bundle savedInstanceState) {
        rows=savedInstanceState.getInt("rows");
        cols=savedInstanceState.getInt("cols");








        mIsMine = (boolean[][]) savedInstanceState.getSerializable("boolean>mines");
        mIsFlagged = (boolean[][]) savedInstanceState.getSerializable("boolean>flags");
        mIsRevealed = (boolean[][]) savedInstanceState.getSerializable("boolean>revealed");

        mMinesSurrounding= (int[][]) savedInstanceState.getSerializable("int>numSurround");
        mImages= (int[][]) savedInstanceState.getSerializable("int>images");
        mChars= (char[][]) savedInstanceState.getSerializable("char>chars");

        notifyDataSetChanged();
    }
}

