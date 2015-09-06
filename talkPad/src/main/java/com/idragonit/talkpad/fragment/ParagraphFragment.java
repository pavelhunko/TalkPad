package com.idragonit.talkpad.fragment;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.editor.Settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ParagraphFragment extends Fragment implements View.OnClickListener {

    ImageButton btn_numbering, btn_bullet;
    ImageButton btn_align_left, btn_align_center, btn_align_right;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_paragraph, container, false);

        /*setup view*/
        initview(view);
        return view;
    }

    private void initview(View view) {
        view.findViewById(R.id.btn_bullet).setOnClickListener(this);
        btn_numbering = (ImageButton) view.findViewById(R.id.btn_numbering);
        btn_numbering.setOnClickListener(this);
        
        btn_bullet = (ImageButton) view.findViewById(R.id.btn_bullet);
        btn_bullet.setOnClickListener(this);
        
        btn_align_left = (ImageButton) view.findViewById(R.id.btn_alignleft);
        btn_align_left.setOnClickListener(this);
        
        btn_align_center = (ImageButton) view.findViewById(R.id.btn_aligncenter);
        btn_align_center.setOnClickListener(this);
        
        btn_align_right = (ImageButton) view.findViewById(R.id.btn_alignright);
        btn_align_right.setOnClickListener(this);
        
        view.findViewById(R.id.btn_indentdecrease).setOnClickListener(this);
        view.findViewById(R.id.btn_indentincrease).setOnClickListener(this);

        setState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bullet:
                onBulletClick(!Settings.IS_BULLET);
                break;
            case R.id.btn_numbering:
                onNumberingClick(!Settings.IS_NUMBERING);
                break;
                
            case R.id.btn_indentdecrease:
                onIndentDecreaseClick();
                break;
            case R.id.btn_indentincrease:
                onIndentIncreaseClick();
                break;
                
            case R.id.btn_alignleft:
                onAlignLeftClick();
                break;
            case R.id.btn_aligncenter:
                onAlignCenterClick();
                break;
            case R.id.btn_alignright:
                onAlignRightClick();
                break;
                
            default:
                return;
        }
    }

    public void setState(){
    	drawBulletButton(Settings.IS_BULLET);
    	drawNumberingButton(Settings.IS_NUMBERING);
    	drawAlignButton(Settings.ALIGNMENT);
    }
    
    private void drawBulletButton(boolean state) {
    	if (btn_bullet!=null){
            if(state)
                btn_bullet.setImageResource(R.drawable.bullet_s);
            else
            	btn_bullet.setImageResource(R.drawable.bullet);
    	}
    }
    
    private void onBulletClick(boolean state) {
    	drawBulletButton(state);
    	Settings.IS_BULLET = state;
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_bullet, 10);
//    	mainFragment.addStyleSpan_Bullet();
    }

    private void drawNumberingButton(boolean state) {
    	if (btn_numbering!=null){
            if(state)
                btn_numbering.setImageResource(R.drawable.numbering_s);
            else
                btn_numbering.setImageResource(R.drawable.numbering);
    	}
    }
    
    private void onNumberingClick(boolean state) {
    	drawNumberingButton(state);
        Settings.IS_NUMBERING = state;
//        mainFragment.addStyleSpan_Numbering();
        AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_numbering, 10);
    }

    private void onIndentDecreaseClick() {
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_indentdecrease, 10);
//    	mainFragment.addStyleSpan_Indent(-1);
    }

    private void onIndentIncreaseClick() {
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_indentincrease, 10);
//    	mainFragment.addStyleSpan_Indent(1);
    }

    private void drawAlignButton(int kind){
    	if (btn_align_left!=null && btn_align_center!=null && btn_align_right!=null){
        	btn_align_left.setImageResource(R.drawable.align_left);
        	btn_align_center.setImageResource(R.drawable.align_center);
        	btn_align_right.setImageResource(R.drawable.align_right);
        	
        	if (kind==Settings.ALIGNMENT_LEFT) 
        		btn_align_left.setImageResource(R.drawable.align_left_s);
        	else if (kind==Settings.ALIGNMENT_CENTER)
        		btn_align_center.setImageResource(R.drawable.align_center_s);
        	else
        		btn_align_right.setImageResource(R.drawable.align_right_s);
    	}
    }
    
    private void onAlignLeftClick() {
    	Settings.ALIGNMENT = Settings.ALIGNMENT_LEFT;
    	drawAlignButton(Settings.ALIGNMENT);
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_alignleft, 10);
//    	mainFragment.addStyleSpan_Align();
    }

    private void onAlignCenterClick(){
    	Settings.ALIGNMENT = Settings.ALIGNMENT_CENTER;
    	drawAlignButton(Settings.ALIGNMENT);
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_aligncenter, 10);
//    	mainFragment.addStyleSpan_Align();
    }

    private void onAlignRightClick(){
    	Settings.ALIGNMENT = Settings.ALIGNMENT_RIGHT;
    	drawAlignButton(Settings.ALIGNMENT);
    	AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_alignright, 10);
//    	mainFragment.addStyleSpan_Align();
    }
}