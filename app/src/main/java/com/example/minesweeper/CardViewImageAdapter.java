package com.example.minesweeper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class CardViewImageAdapter extends RecyclerView.Adapter<CardViewImageAdapter.ViewHolder>
{
    private static OIClickAndLongClickListener sOIClickAndLongClickListener;
    private final Context mContext;
    private final int[] mImages;
    private final char[] mChars;

    private final double mScaleHeightP, mScaleHeightL = 20;
    private final int mINVALID_FLAG = -99;


    public CardViewImageAdapter (Context context, int numberOfSpaces, @SuppressWarnings (
            "SameParameterValue") int defaultDrawableID, double scaleVertical)
    {
        mContext = context;

        mScaleHeightP = scaleVertical;

        mImages = new int[numberOfSpaces];
        mChars = new char[numberOfSpaces];

        fillMemberArrays (defaultDrawableID);
    }

    private void fillMemberArrays (int defaultDrawableID)
    {
        for (int i = 0; i < mImages.length; i++) {
            mImages[i] = defaultDrawableID;
            mChars[i] = ' ';
        }
    }

    public void setOnItemClickAndLongClickListener(OIClickAndLongClickListener oiClickAndLongClickListener)
    {
        CardViewImageAdapter.sOIClickAndLongClickListener = oiClickAndLongClickListener;
    }

    public void setChar(int position, char bombsNearby) {
        mChars[position] = bombsNearby;
        notifyDataSetChanged();
    }

    public void setImage (int position, int newDrawableID)
    {
        mImages[position] = newDrawableID;

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
        currentLayoutParams.height = getHeightSize ();

        // Set the LP of this ImageView to point to that newly-adjusted LP with adjusted height
        buttonInNewlyInflatedView.setLayoutParams (currentLayoutParams);
    }

    @Override public void onBindViewHolder (CardViewImageAdapter.ViewHolder holder, int position)
    {
        Button currentButton = holder.mCurrentButton;
        Drawable currentImage = ContextCompat.getDrawable(mContext,mImages[position]);
        char currentChar = mChars[position];

        currentButton.setBackgroundDrawable (currentImage);
        currentButton.setText(String.valueOf(currentChar));
        //currentButton.setLongClickable(true);
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
        return mImages.length;
    }

    @Override
    public long getItemId (int position)
    {
        return position >= 0 && position < mImages.length ? mImages[position] : -1;
    }

    public int[] getDataOfModel ()
    {
        return mImages.clone ();
    }

    public char[] getSecondaryDataOfModel() {
        return mChars.clone();
    }

/*    public int [] getSecondaryDataOfModel()
    {
        return mImageTints.clone();
    }*/

    // Inner Class - references a RecyclerView View item
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
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
            sOIClickAndLongClickListener.onItemLongClick (getAdapterPosition (), v);
            return true;
        }

        @Override
        public void onClick (View v)
        {
            sOIClickAndLongClickListener.onItemClick (getAdapterPosition (), v);
        }
    }

    // used to send data out of Adapter - implemented in the calling Activity/Fragment
    @SuppressWarnings ("UnusedParameters")
    public interface OIClickAndLongClickListener
    {
        void onItemLongClick (int position, View v);
        void onItemClick (int position, View v);
    }
}

