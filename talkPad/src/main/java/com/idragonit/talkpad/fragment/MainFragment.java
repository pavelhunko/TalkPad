package com.idragonit.talkpad.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.adapter.PagerAdapter;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.editor.SpeechCommand.Item;
import com.idragonit.talkpad.editor.TNoteEditText;
import com.idragonit.talkpad.editor.data.TNoteData;
import com.idragonit.talkpad.editor.data.TNoteDataUtil;
import com.idragonit.talkpad.editor.style.TBulletStyleSpan;
import com.idragonit.talkpad.ui.LanguagePickerDialog;
import com.idragonit.talkpad.ui.ResponsiveScrollView;
import com.idragonit.talkpad.utils.CommonMethods;
import com.idragonit.talkpad.utils.Constants;
import com.idragonit.talkpad.utils.ImageFilePath;
import com.idragonit.talkpad.utils.Stack;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;

public class MainFragment extends Fragment implements View.OnTouchListener,
        TextWatcher, Constants, LanguagePickerDialog.LanguagePickerListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static Boolean vis;
    private static String TAG = "MainFragment";
    static private TNoteData mNote; // g
    //    private static FloatingActionMenu mFloatingActionMenu;
    protected TNoteEditText mNoteEdit; // a
    MainFragment instance;
    FontFragment mFontFragment;
    ParagraphFragment mParagraphFragment;
    PhotoFragment mPhotoFragment;
    public Handler mNoteMsgHandler = new Handler() {
        public final void handleMessage(Message message) {

            super.handleMessage(message);

            if (message.what == 1) {

            } else if (message.what == 2) {
                mNoteEdit.bringPointIntoView(mNoteEdit.getSelectionStart());
            } else if (message.what == 2000) {
                if (mFontFragment != null && mParagraphFragment != null
                        && mPhotoFragment != null) {
                    TNoteEditText.TNoteParagraphStyle b = mNoteEdit
                            .updateParagraphStyle();

                    Settings.IS_BOLD = b.bold;
                    Settings.IS_ITALIC = b.italic;
                    Settings.IS_UNDERLINE = b.underline;

                    Settings.TEXT_SIZE = b.textSize;
                    Settings.COLOR = b.foregroundColor;

                    Settings.ALIGNMENT = ALIGNMENT_LEFT;
                    if (b.textAlign == Layout.Alignment.ALIGN_CENTER)
                        Settings.ALIGNMENT = ALIGNMENT_CENTER;
                    else if (b.textAlign == Layout.Alignment.ALIGN_OPPOSITE)
                        Settings.ALIGNMENT = ALIGNMENT_RIGHT;

                    Settings.IS_BULLET = b.mBulletType == 2;
                    Settings.IS_NUMBERING = b.mBulletType == 1;
                    Settings.FONT_NAME = b.fontFamilyUri;

                    mFontFragment.setState();
                    mParagraphFragment.setState();
                } else {
                    mNoteMsgHandler.sendMessageDelayed(message,
                            ACTION_DELAY_TIME);
                }

                Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                Settings.TEMP_TEXT = mNoteEdit.getText().toString();

            } else if (message.what == 2001) {
                String fileName = (String) message.obj;

                if (!fileName.startsWith("/mnt")) {
                    fileName = String.valueOf(mNote.getNotePath()) + fileName;
                }

                Intent intent = new Intent();
                File file = new File(fileName);

                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),
                        TNoteDataUtil.getFileType(file));

                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "cannot find app",
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    };
    CommandsDialogFragment mCommandsDialogFragment;
    View btnMenu;
    LinearLayout mLayoutCommand = null;
    LinearLayout mLayoutToolbar = null;
    RelativeLayout mLayoutPager = null;
    RelativeLayout mLayoutHeader = null;
    ResponsiveScrollView mScrollView = null;
    KeyboardPopupListener key_listener;
    LanguagePickerDialog languagePickerDialog;
    Handler mCommandClickListener = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent;

            mNoteEdit.requestFocus();
            mNoteEdit.bringPointIntoView(mNoteEdit.getSelectionStart());
            // showColorSizePanel(false, 0);

            switch (msg.what) {
                case ACTION_GALLERY:
                    String filePath = msg.obj.toString();
                    String newFilePath = TNoteDataUtil.copyFile(filePath,
                            mNote.getNotePath());

                    if (newFilePath != null) {
                        try {
                            mNoteEdit.setSelection(Settings.SELECTION_START,
                                    Settings.SELECTION_END);
                        } catch (Exception ee) {
                        }

                        if (mNoteEdit.insertBitmap(newFilePath)) {
                            Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                        }
                    }
                    break;

                case ACTION_CAMERA:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    if (mNoteEdit.insertBitmap(AppConstants.PICTURE_NAME)) {
                        Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                    }

                    AppConstants.PICTURE_NAME = "";
                    break;

                case ACTION_FONTNAME:
                    Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                    Settings.SELECTION_END = mNoteEdit.getSelectionEnd();

                    if (mFontFragment != null) {
                        mFontFragment.onChangecaseClick();
                    }
                    break;

                case ACTION_FONTCOLOR:
                    Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                    Settings.SELECTION_END = mNoteEdit.getSelectionEnd();

                    // showColorSizePanel(true, 1);
                    // if (mFontFragment!=null) {
                    // mFontFragment.onChangeColorClick();
                    // }

                    if (mFontFragment != null) {
                        mFontFragment.onChangeColorClick();
                    }
                    break;

                case ACTION_FONTSIZE:
                    Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                    Settings.SELECTION_END = mNoteEdit.getSelectionEnd();

                    if (mFontFragment != null) {
                        mFontFragment.onAdjustFontSizeClick();
                    }
                    break;

                case ACTION_SPEECH:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    Bundle data = msg.getData();
                    ArrayList<String> list = data.getStringArrayList(VOICE_DATA);
                    String all_text = "";

                    // for (String str : list) {
                    // all_text += str + " ";
                    // }

                    if (list.size() > 0) {
                        all_text = list.get(0);
                        if (!mNoteEdit
                                .isFirstPositionOfLine(Settings.SELECTION_START))
                            all_text = " " + all_text;

                        mNoteEdit.addText(all_text);
                    }
                    break;

                case ACTION_COMMAND:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    Bundle c_data = msg.getData();
                    ArrayList<String> c_list = c_data
                            .getStringArrayList(VOICE_DATA);
                    if (c_list.size() > 0) {
                        analyzeCommand(c_list);
                    }
                    break;

                case ACTION_TEXT_COPY:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    mNoteEdit.onTextContextMenuItem(android.R.id.copy);
                    break;

                case ACTION_TEXT_CUT:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    mNoteEdit.onTextContextMenuItem(android.R.id.cut);
                    break;

                case ACTION_TEXT_PASTE:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    mNoteEdit.onTextContextMenuItem(android.R.id.paste);
                    break;

                case ACTION_TEXT_DELETE:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }

                    mNoteEdit.addText("");
                    break;

                case R.id.btn_addphoto:
                    Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                    Settings.SELECTION_END = mNoteEdit.getSelectionEnd();

                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");

                    try {
                        startActivityForResult(intent, ACTIVITY_RESULT__GALLERY);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.btn_takepicture:
                    Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                    Settings.SELECTION_END = mNoteEdit.getSelectionEnd();

                    Date date = new Date();
                    String filename = (new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss"))
                            .format(date) + ".jpeg";
                    File file = new File(mNote.getNotePath() + filename);

                    AppConstants.PICTURE_NAME = file.toString();

                    intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra("output", Uri.fromFile(file));

                    try {
                        startActivityForResult(intent, ACTIVITY_RESULT__CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;

                // case R.id.edit_insert_voice:
                //
                // intent = new Intent();
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // intent.setType("audio/amr");
                //
                // try {
                // startActivityForResult(intent, 3);
                // } catch (ActivityNotFoundException e) {
                // e.printStackTrace();
                // }
                // break;
                //
                // case R.id.edit_insert_line:
                // mNoteEdit.commandInsertLine();
                // break;
                //
                // case R.id.edit_clear_style:
                // mNoteEdit.commandClearStyle();
                // break;

                case R.id.btn_numbering:
                    mNoteEdit.commandToggleBullet(
                            // bb1.textAlign,
                            // Layout.Alignment.ALIGN_NORMAL,
                            TBulletStyleSpan.TBulletType.SORT,
                            TBulletStyleSpan.TBulletSortType.SORT_1,
                            TBulletStyleSpan.TBulletShapeType.SHAPE_CIRCLE);
                    break;

                case R.id.btn_bullet:
                    mNoteEdit.commandToggleBullet(
                            // bb2.textAlign,
                            // Layout.Alignment.ALIGN_NORMAL,
                            TBulletStyleSpan.TBulletType.SHAPE,
                            TBulletStyleSpan.TBulletSortType.SORT_1,
                            TBulletStyleSpan.TBulletShapeType.SHAPE_DISC);
                    break;

                case R.id.btn_alignleft:
                    mNoteEdit.commandToggleAlignment(Layout.Alignment.ALIGN_NORMAL);
                    // Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                    // mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
                    break;

                case R.id.btn_aligncenter:
                    mNoteEdit.commandToggleAlignment(Layout.Alignment.ALIGN_CENTER);
                    // Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                    // mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
                    break;

                case R.id.btn_alignright:
                    mNoteEdit
                            .commandToggleAlignment(Layout.Alignment.ALIGN_OPPOSITE);
                    // Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
                    // mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
                    break;

                case R.id.btn_indentdecrease:
                    mNoteEdit.commandIndent(false);
                    break;

                case R.id.btn_indentincrease:
                    mNoteEdit.commandIndent(true);
                    break;

                case R.id.btn_bold:
                    mNoteEdit.commandToggleBold();
                    break;

                case R.id.btn_underline:
                    mNoteEdit.commandToggleUnderline();
                    break;

                case R.id.btn_italic:
                    mNoteEdit.commandToggleItalic();
                    break;

                case R.id.btn_changecolor:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }
                    // showColorSizePanel(true, 1);
                    mNoteEdit.commandSetTextColor(Settings.COLOR);
                    break;

                // case R.id.edit_set_textbkcolor:
                // showColorSizePanel(true, 2);
                // break;
                case R.id.btn_changecase: // fontname
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }
                    mNoteEdit.commandSetFontface(Settings.FONT_NAME);
                    break;

                case R.id.btn_adjustsize:
                    try {
                        mNoteEdit.setSelection(Settings.SELECTION_START,
                                Settings.SELECTION_END);
                    } catch (Exception ee) {
                    }
                    // showColorSizePanel(true, 3);
                    mNoteEdit.commandSetTextSize(Settings.TEXT_SIZE);
                    break;

                // // case R.id.edit_save:
                // // if (mModified == 0) {
                // // a(1);
                // // } else {
                // // MainActivity.a(this.a);
                // // finish();
                // // }
                //
                // case R.id.edit_insert_www:
                // case R.id.edit_root:
                default:
                    break;

            }
        }

        ;
    };
    private FloatingActionButton fSpeechButton;
    private FloatingActionButton fCommandButton;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean isListening;
    private ImageButton mUndoBtn;
    private ImageButton mRedoBtn;
    private ImageView mSettingBtn;
    private boolean isOperating = true;
    private FloatingActionMenu mFloatingActionMenu;

    @Override
    public void onResume() {
        super.onResume();
        //setFloatingActionMenuVisibility();
    }

    public synchronized Boolean setFloatingActionMenuVisibility() {
        return instance.getUserVisibleHint();
    }

    @Override
    public void onPause() {
        super.onPause();
//        setFloatingActionMenuVisibility();
    }

    private int dpToPx(int dp) {
        float density = getActivity().getApplicationContext().getResources()
                .getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle args) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        String filename = APP_NAME;
        if (AppConstants.FILE_PATH.length() == 0) {

        } else {
            File file = new File(AppConstants.FILE_PATH);
            if (file != null && file.exists() && file.isFile()) {
                filename = file.getName();
                filename = filename.substring(0,
                        filename.lastIndexOf(FILE_EXTENTION));
            }
        }

        TextView header = (TextView) view.findViewById(R.id.header_text);
        header.setText(filename);

        btnMenu = view.findViewById(R.id.btn_left);
        btnMenu.setOnTouchListener(this);

        mLayoutToolbar = (LinearLayout) view.findViewById(R.id.layout_toolbar);
        mLayoutPager = (RelativeLayout) view.findViewById(R.id.layout_pager);
        mLayoutHeader = (RelativeLayout) view.findViewById(R.id.layout_header);

        mScrollView = (ResponsiveScrollView) view
                .findViewById(R.id.scroll_view);

        mUndoBtn = (ImageButton) view.findViewById(R.id.btn_undo);
        mRedoBtn = (ImageButton) view.findViewById(R.id.btn_redo);
        mNoteEdit = (TNoteEditText) view.findViewById(R.id.editor);
        mSettingBtn = (ImageView) view.findViewById(R.id.btn_setting);

        mFloatingActionMenu = (FloatingActionMenu) view.findViewById(R.id.floating_action_menu);

        fSpeechButton = (FloatingActionButton) view.findViewById(R.id.floating_menu_speech);
        fCommandButton = (FloatingActionButton) view.findViewById(R.id.floating_menu_command);

        fSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                Settings.SELECTION_END = mNoteEdit.getSelectionEnd();
                if (!isListening)
                    speak(ACTIVITY_RESULT__VOICE_SPEECH);

            }
        });

        fCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.SELECTION_START = mNoteEdit.getSelectionStart();
                Settings.SELECTION_END = mNoteEdit.getSelectionEnd();
                speak(ACTIVITY_RESULT__VOICE_COMMAND);
            }
        });


        if (AppConstants.LINE_NUMBER) {
            mNoteEdit.setPadding(dpToPx(25), dpToPx(3), dpToPx(10), dpToPx(6));
        } else {
            mNoteEdit.setPadding(dpToPx(12), dpToPx(3), dpToPx(10), dpToPx(6));
        }

        mSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IconContextMenu menu = new IconContextMenu(getActivity(),
                        R.menu.setting);
                MenuItem m = menu.getMenu().getItem(1);
                m.setIcon(getResources().getDrawable(
                        AppConstants.LINE_NUMBER ? R.drawable.menu_check
                                : R.drawable.menu_uncheck));

                menu.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {
                    @Override
                    public void onIconContextItemSelected(MenuItem item,
                                                          Object info) {
                        // TODO Auto-generated method stub
                        switch (item.getItemId()) {
                            case R.id.mnu_language:
                                onSpeechLanguage();
                                break;

                            case R.id.mnu_linenumber:
                                onLineNumber();
                                break;

                            case R.id.mnu_listofcomands:
                                onListOfCommands();
                                break;
                        }
                    }
                });

                menu.show();
            }
        });

        mUndoBtn.setOnTouchListener(this);
        mRedoBtn.setOnTouchListener(this);

        instance = this;
        initView(view);

        key_listener = new KeyboardPopupListener() {
            @Override
            public void onShow() {
                // TODO Auto-generated method stub
                if (mLayoutCommand != null)
                    mLayoutCommand.setVisibility(View.GONE);

            }

            @Override
            public void onHide() {
                if (mLayoutCommand != null)
                    mLayoutCommand.setVisibility(View.VISIBLE);


            }
        };

        AppConstants.COMMAND_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                if (msg.what == ACTION_GALLERY || msg.what == ACTION_CAMERA) {
                    Message new_msg = new Message();
                    new_msg.obj = msg.obj;
                    new_msg.what = msg.what;
                    instance.mCommandClickListener.sendMessage(new_msg);

                } else if (msg.what == ACTION_SPEECH
                        || msg.what == ACTION_COMMAND) {
                    Message new_msg = new Message();
                    new_msg.what = msg.what;
                    new_msg.setData(msg.getData());
                    instance.mCommandClickListener.sendMessage(new_msg);

                } else {
                    instance.mCommandClickListener.sendEmptyMessage(msg.what);
                }
            }
        };

        mNoteEdit.measure(MeasureSpec.AT_MOST, MeasureSpec.AT_MOST);
        mScrollView.setKeyboardPopupListener(key_listener);

        return view;
    }

    private void onListOfCommands() {
        //create new dialog with empty text box
        mCommandsDialogFragment = new CommandsDialogFragment();
        mCommandsDialogFragment.show(getActivity().getSupportFragmentManager(), "CommandsDialogFragment");
    }

    public void onSpeechLanguage() {
        languagePickerDialog = LanguagePickerDialog
                .getInstance(MainFragment.this);
        languagePickerDialog.setLanguage(AppConstants.SPEECH_LANGUAGE);
        languagePickerDialog.show(getActivity().getSupportFragmentManager(),
                "LanguagePicker");
    }

    public void onLineNumber() {
        AppConstants.LINE_NUMBER = !AppConstants.LINE_NUMBER;

        try {
            SharedPreferences pref = getActivity().getApplicationContext()
                    .getSharedPreferences(APP_NAME, getActivity().MODE_PRIVATE);

            Editor editor = pref.edit();
            editor.putBoolean(Constants.PREFERENCE_LINENUMBER,
                    AppConstants.LINE_NUMBER);
            editor.commit();
        } catch (Exception f) {
        }

        if (AppConstants.LINE_NUMBER) {
            mNoteEdit.setPadding(dpToPx(25), dpToPx(3), dpToPx(10), dpToPx(6));
        } else {
            mNoteEdit.setPadding(dpToPx(12), dpToPx(3), dpToPx(10), dpToPx(6));
        }
    }

    private void initView(View root) {
        // TODO Auto-generated method stub
        CommonMethods.setupUI(getActivity(), root.findViewById(R.id.lay_root));

        List<Fragment> fragments = new Vector<Fragment>();

        mFontFragment = new FontFragment();
        mParagraphFragment = new ParagraphFragment();
        mPhotoFragment = new PhotoFragment();
        mCommandsDialogFragment = new CommandsDialogFragment();

        fragments.add(mFontFragment);
        fragments.add(mParagraphFragment);
        fragments.add(mPhotoFragment);

        PagerAdapter mPagerAdapter = new PagerAdapter(
                getChildFragmentManager(), fragments);
        ViewPager mViewPager = (ViewPager) root
                .findViewById(R.id.toolbar_pager);
        mViewPager.setAdapter(mPagerAdapter);

        CirclePageIndicator mIndicator = (CirclePageIndicator) root
                .findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);

        if (Settings.TEMP_NOTES.length() > 0) {
            mNote = new TNoteData();
            mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
            Settings.TEMP_TEXT = mNoteEdit.getText().toString();

            if (AppConstants.IS_OPENED) {
                Settings.BEFORE_NOTES = Settings.TEMP_NOTES;
                AppConstants.IS_OPENED = false;
            }
        } else {
            mNote = TNoteData.newNote(null);
            mNoteEdit.setText("");
            Stack.clear();

            Settings.TEMP_NOTES = mNote.getHtmlNote(mNoteEdit);
            Stack.add(Settings.TEMP_NOTES);

            Settings.TEMP_TEXT = mNoteEdit.getText().toString();
            Settings.BEFORE_NOTES = Settings.TEMP_NOTES;
            // Stack.setEmptyString(mNote.getHtmlNote(mNoteEdit));
        }

        AppConstants.EDIT_MODE = OPEN_MODE;

        if (mNoteEdit != null) {
            CommonMethods.hideKeyboard(getActivity(), mNoteEdit);
        }

        mNoteEdit.setFocusable(true);
        mNoteEdit.requestFocus();
        mNoteEdit.addTextChangedListener(this);

        mNoteEdit.setHandler(mNoteMsgHandler);
        mNoteMsgHandler.sendMessageDelayed(mNoteMsgHandler.obtainMessage(1),
                100L);

        AppConstants.IS_CREATED = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        String html = "";

        switch (v.getId()) {
            case R.id.btn_left:
                if (mNoteEdit != null) {
                    CommonMethods.hideKeyboard(getActivity(), mNoteEdit);
                }

                AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_MENU,
                        ACTION_DELAY_TIME);
                break;

            case R.id.btn_undo:
                html = Stack.undo();
                if (html.length() > 0) {
                    isOperating = true;
                    Settings.TEMP_NOTES = html;
                    if (mNoteEdit != null) {
                        mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
                    }
                }
                break;

            case R.id.btn_redo:
                html = Stack.redo();
                if (html.length() > 0) {
                    isOperating = true;
                    Settings.TEMP_NOTES = html;
                    if (mNoteEdit != null) {
                        mNote.loadFromHtml(mNoteEdit, Settings.TEMP_NOTES);
                    }
                }
                break;

        }

        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mSpeechRecognizer = SpeechRecognizer
                .createSpeechRecognizer(getActivity());
        mSpeechRecognizerIntent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                AppConstants.SPEECH_LANGUAGE);
        mSpeechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity()
                        .getPackageName());

        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);

    }

    public void speak(int command) {


        if (!isListening) {

            try {
                startActivityForResult(mSpeechRecognizerIntent, command);
                isListening = true;
            } catch (Exception ee) {
            }
        }
    }

    @Override
    public void onDestroy() {
        if (!mSpeechRecognizer.equals(null)) {
            mSpeechRecognizer.destroy();
            isListening = false;
        }
        super.onDestroy();
    }

    private String getFilePathFromActivityResultUri(Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;

        if (res == null || res.length() == 0) {
            res = ImageFilePath.getPath(getActivity(), uri);
        }

        return res;
    }

    private String getFilePathFromActivityResult(Uri uri) {
        String res = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().managedQuery(uri, proj, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int i1 = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(i1);
            }
            if (cursor != null)
                cursor.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }

        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);
        isListening = false;

        if (resultCode != getActivity().RESULT_OK)
            return;

        Uri uri = null;

        if (intent != null) {
            uri = intent.getData();
        }

        switch (requestCode) {
            case ACTIVITY_RESULT__GALLERY:
                if (uri != null) {
                    String filePath = getFilePathFromActivityResultUri(uri);
                    if (filePath != null) {
                        Message msg = new Message();
                        msg.what = ACTION_GALLERY;
                        msg.obj = filePath;
                        AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                                ACTION_DELAY_TIME);
                    }
                }
                break;

            case ACTIVITY_RESULT__CAMERA:
                if (uri == null && AppConstants.PICTURE_NAME.length() > 0) {
                    uri = Uri.fromFile(new File(AppConstants.PICTURE_NAME));
                }

                if (uri != null) {
                    Message msg = new Message();
                    msg.what = ACTION_CAMERA;
                    msg.obj = AppConstants.PICTURE_NAME;
                    AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                            ACTION_DELAY_TIME);
                }
                break;

            case ACTIVITY_RESULT__VOICE_SPEECH:
                try {
                    Message msg = new Message();
                    msg.what = ACTION_SPEECH;
                    Bundle data = new Bundle();
                    ArrayList<String> textMatchList = intent
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    data.putStringArrayList(VOICE_DATA, textMatchList);
                    msg.setData(data);

                    if (textMatchList.size() > 0)
                        AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                                ACTION_DELAY_TIME);
                } catch (Exception ee) {
                }
                break;

            case ACTIVITY_RESULT__VOICE_COMMAND:
                try {
                    Message msg = new Message();
                    msg.what = ACTION_COMMAND;
                    Bundle data = new Bundle();
                    ArrayList<String> textMatchList = intent
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    data.putStringArrayList(VOICE_DATA, textMatchList);
                    msg.setData(data);

                    if (textMatchList.size() > 0)
                        AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                                ACTION_DELAY_TIME);
                } catch (Exception ee) {
                }
                break;

            default:
                return;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence paramCharSequence,
                                  int paramInt1, int paramInt2, int paramInt3) {
        // TODO Auto-generated method stub

    }

    protected void analyzeCommand(ArrayList<String> c_list) {
        // TODO Auto-generated method stub

        try {
            mNoteEdit.setSelection(Settings.SELECTION_START,
                    Settings.SELECTION_END);
        } catch (Exception ee) {
        }

        boolean is_find = false;

        for (int i = Settings.COMMAND.list.size() - 1; i >= 0; i--) {
            Item item = Settings.COMMAND.list.get(i);
            item.init();

            for (String voice : c_list) {
                if (item.check(voice)) {

                    Log.e("Analyze Command", voice);
                    if (item.type == COMMAND__SELECT_BUTTON
                            || item.type == COMMAND__SELECT_BUTTON__COMBINE
                            || item.type == COMMAND__SELECT_BUTTON__COMPLEX) {
                        Log.e("Command", "Line (" + item.line1 + "~"
                                + item.line2 + ")");
                        int iStart = -1, iEnd = -1;
                        if (item.isStart) {
                            iStart = 0;
                        } else {
                            iStart = mNoteEdit.getFirstIndexOfLine(item.line1);
                        }

                        if (item.isEnd) {
                            iEnd = mNoteEdit.getText().toString().length();
                        } else {
                            iEnd = mNoteEdit.getLastIndexOfLine(item.line2);
                        }

                        try {
                            if (iStart != -1 && iEnd == -1) {
                                iEnd = iStart;
                            }
                            if (iEnd != -1 && iStart == -1) {
                                iStart = iEnd;
                            }

                            if (iStart > iEnd) {
                                int w = iStart;
                                iStart = iEnd;
                                iEnd = w;
                            }

                            Log.e("Command", "Line2 (" + iStart + "~" + iEnd
                                    + ")");

                            if (iStart != -1 && iEnd != -1) {
                                Settings.SELECTION_START = iStart;
                                Settings.SELECTION_END = iEnd;
                                mNoteEdit.setSelection(
                                        Settings.SELECTION_START,
                                        Settings.SELECTION_END);
                            }
                        } catch (Exception ee) {
                        }

                        switch (item.action) {
                            case R.id.btn_changecolor:
                                if (item.color == -1)
                                    mCommandClickListener
                                            .sendEmptyMessage(ACTION_FONTCOLOR);
                                else {
                                    Settings.COLOR = item.color;
                                    mCommandClickListener
                                            .sendEmptyMessage(item.action);
                                }
                                break;

                            case R.id.btn_adjustsize:
                                if (item.size == -1)
                                    mCommandClickListener
                                            .sendEmptyMessage(ACTION_FONTSIZE);
                                else {
                                    Settings.TEXT_SIZE = item.size;
                                    mCommandClickListener
                                            .sendEmptyMessage(item.action);
                                }
                                break;

                            case R.id.btn_changecase:
                                if (item.font == null) {
                                    mCommandClickListener
                                            .sendEmptyMessage(ACTION_FONTNAME);
                                } else {
                                    Settings.FONT_NAME = item.font;
                                    mCommandClickListener
                                            .sendEmptyMessage(item.action);
                                }
                                break;

                            default:
                                mCommandClickListener.sendEmptyMessage(item.action);
                                break;
                        }

                        is_find = true;
                        break;
                    }

                    if (item.type == COMMAND__SELECT_MENU
                            || item.type == COMMAND__SELECT_MENU__COMBINE
                            || item.type == COMMAND__SELECT_MENU__COMPLEX) {
                        Message msg = new Message();
                        msg.what = ACTION_OPEN_PAD;
                        msg.arg1 = item.action;
                        msg.arg2 = 1;
                        AppConstants.APP_HANDLER.sendMessageDelayed(msg,
                                ACTION_DELAY_TIME);

                        is_find = true;
                        break;
                    }
                }
            }

            if (is_find)
                break;
        }

        if (!is_find) {
            Toast.makeText(getActivity(), "Command is invalid or unavailable!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTextChanged(CharSequence paramCharSequence, int paramInt1,
                              int paramInt2, int paramInt3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable paramEditable) {
        // TODO Auto-generated method stub
        if (isOperating) {
            isOperating = false;
        } else {
            if (mNoteEdit != null)
                Stack.add(mNote.getHtmlNote(mNoteEdit));
        }
    }

//    @Override
//    public void onItemClick(final FloatingActionMenu floatingActionMenu, final int i) {
//        //Log.i(TAG, "onItemClick: " + i);
//        //Toast.makeText(getActivity(), "item click " + i, Toast.LENGTH_SHORT).show();
//        switch (i) {
//            case 0:
//                Settings.SELECTION_START = mNoteEdit.getSelectionStart();
//                Settings.SELECTION_END = mNoteEdit.getSelectionEnd();
//                if (!isListening)
//                    speak(ACTIVITY_RESULT__VOICE_SPEECH);
//
//                break;
//
//            case 1:
//                Settings.SELECTION_START = mNoteEdit.getSelectionStart();
//                Settings.SELECTION_END = mNoteEdit.getSelectionEnd();
//                speak(ACTIVITY_RESULT__VOICE_COMMAND);
//                break;
//        }
//    }

    @Override
    public void onLanguagePickerDoneClick(DialogFragment dialog) {
        // TODO Auto-generated method stub
        if (languagePickerDialog != null) {
            AppConstants.SPEECH_LANGUAGE = languagePickerDialog.getLanguage();

            try {
                SharedPreferences pref = getActivity().getApplicationContext()
                        .getSharedPreferences(APP_NAME,
                                getActivity().MODE_PRIVATE);

                Editor editor = pref.edit();
                editor.putString(Constants.PREFERENCE_SPEECHLANGUAGE,
                        AppConstants.SPEECH_LANGUAGE);
                editor.commit();
            } catch (Exception f) {
            }

            languagePickerDialog.dismiss();
        }
    }

    public interface KeyboardPopupListener {
        public void onShow();

        public void onHide();
    }

    protected class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub
            Log.i(TAG, "Speech began");

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
            Log.i(TAG, "Speech ended");
            isListening = false;

        }

        @Override
        public void onError(int error) {
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        }

        @Override
        public void onResults(Bundle results) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onResults");
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub

        }
    }

}
