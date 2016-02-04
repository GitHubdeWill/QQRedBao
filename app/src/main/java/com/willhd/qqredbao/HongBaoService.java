package com.willhd.qqredbao;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class HongBaoService extends AccessibilityService {
    private final static String TAG = "WillHo";

    private final static String QQ_NORMAL_OPEN = "点击拆开";
    private final static String QQ_NORMAL_OPENED = "已拆开";
    private final static String QQ_PASSWD_OPEN = "口令红包";
    private final static String QQ_PASSWD_COPY = "点击输入口令";
    private final static String QQ_SEND = "发送";
    private final static String QQ_NOTIFIED = "QQ红包";
    private final static String[] QQ_GUA = {"我是挂", "我要被踢了"};//Can add more key words

    private boolean hongBaoFound;
    private String lastID;
    private boolean gotIt;
    private boolean copied;
    private boolean dangerous = false;
    private long lastCheckedtime = Long.MAX_VALUE;

    private AccessibilityNodeInfo rootNodeInfo;
    private List<AccessibilityNodeInfo> nodeInfos;

    @Override
    public void onAccessibilityEvent (AccessibilityEvent event){
        lastID = "";
        gotIt = false;
        copied = false;
        dangerous = true;

        Log.e(TAG, "HBService Started.");
        this.rootNodeInfo = event.getSource();
        if (this.rootNodeInfo == null) return;

        //boolean entered = searchNotificationAndClick();
        if(/*entered &&*/ ((event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)))searchHongBao();

        //if ((lastCheckedtime == Long.MAX_VALUE) || (System.currentTimeMillis()-lastCheckedtime >= 5000)) {
            //uncomment to enable anti-add-on detection
            /*if (!this.findNodeInfo(this.rootNodeInfo, QQ_GUA).isEmpty()) {
                Log.e(TAG, "Sensitive Words detected. Action blocked");
                dangerous = true;
                lastCheckedtime = System.currentTimeMillis();
                hongBaoFound = false;
                return;
            }
            else dangerous = false;*/
        //}

        if((hongBaoFound)&&(nodeInfos != null)){
            if (getRootInActiveWindow() != null) recycle(getRootInActiveWindow());
        }

        List<AccessibilityNodeInfo> temp = this.findNodeInfo(this.rootNodeInfo, new String[]{
                "已存入余额", "来晚一步，红包被领完了"
        });

        if ((!temp.isEmpty())) {
            //this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            gotIt = false;
            temp.clear();
        }

    }


    public void recycle(AccessibilityNodeInfo info) {
        if (info == null) return;

        if (info.getChildCount() == 0) {
            Log.e(TAG, "child:" + info.getClassName());
            Log.e(TAG, "Dialog:" + info.canOpenPopup());
            Log.e(TAG, "Text：" + info.getText());
            Log.e(TAG, "windowId:" + info.getWindowId());
            Log.e(TAG, "ViewId:" + info.getViewIdResourceName());

            Log.e(TAG, "Currently it's recycling " + dangerous);

            if (info.getText() != null && info.getText().toString().equals(QQ_NORMAL_OPEN)) {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e(TAG, "Got it");
                gotIt = true;
            }

            //Uncomment to enable password Lucky Money
            /*if ((info.getText() != null && info.getText().toString().equals(QQ_PASSWD_COPY)) && !dangerous) {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                copied = true;
            }

            if ((info.getText() != null && info.getText().toString().equals(QQ_PASSWD_OPEN) && !dangerous)) {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if (info.getClassName().toString().equals("android.widget.Button") && info.getText().toString().equals(QQ_SEND) && copied) {
                copied = false;
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if (info.getText() != null && info.getText().toString().contains(QQ_NOTIFIED)) {
                Log.e(TAG, "Found notified");
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }*/

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    /*not implemented
    private boolean searchNotificationAndClick(){
        Log.e(TAG, "Searching Notification");
        if (this.rootNodeInfo == null) return false;

        List<AccessibilityNodeInfo> temp = this.findNodeInfo(this.rootNodeInfo, new String[]{
                QQ_NOTIFIED
        });

        if(!temp.isEmpty()){
            Log.e(TAG, "Notification Found");
            if (getRootInActiveWindow() != null) recycle(getRootInActiveWindow());
            return true;

        }
        return false;
    }*/

    private void searchHongBao(){
        Log.e(TAG, "Search started");
        if (this.rootNodeInfo== null) return;

        List<AccessibilityNodeInfo> temp = this.findNodeInfo(this.rootNodeInfo, new String[]{
            QQ_NORMAL_OPEN, QQ_PASSWD_OPEN, QQ_PASSWD_COPY
        });

        if(!temp.isEmpty()){
            Log.e(TAG, "Currently Temp is not empty");
            String nodeID = Integer.toHexString(System.identityHashCode(this.rootNodeInfo));
            if (!nodeID.equals(lastID)){
                this.hongBaoFound = true;
                this.nodeInfos = temp;
                this.lastID = nodeID;
            }
        }
    }

    private List<AccessibilityNodeInfo> findNodeInfo(AccessibilityNodeInfo info, String[] texts){
        for (int i = 0; i < texts.length; i++){
            List<AccessibilityNodeInfo> nodes = info.findAccessibilityNodeInfosByText(texts[i]);

            if (!nodes.isEmpty()) return nodes;
        }
        return new ArrayList<>();
    }

    @Override
    public void onInterrupt () {

    }
}
