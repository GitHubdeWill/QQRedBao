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
    private final static String QQ_NOTIFIED = "有关注内容";

    private boolean hongBaoFound;
    private String lastID;

    private AccessibilityNodeInfo rootNodeInfo;
    private List<AccessibilityNodeInfo> nodeInfos;

    @Override
    public void onAccessibilityEvent (AccessibilityEvent event){
        Log.e(TAG, "HBService Started.");
        this.rootNodeInfo = event.getSource();
        if (this.rootNodeInfo == null) return;

        boolean entered = searchNotificationAndClick();
        if(entered && ((event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)))searchHongBao();

        if((hongBaoFound)&&(nodeInfos != null)){

        }

        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);


    }

    //need to be implemented
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
//            Log.e(TAG, "child widget----------------------------" + info.getClassName());
//            Log.e(TAG, "showDialog:" + info.canOpenPopup());
//            Log.e(TAG, "Text：" + info.getText());
//            Log.e(TAG, "windowId:" + info.getWindowId());

            if (info.getText() != null && info.getText().toString().equals(QQ_PASSWD_COPY)) {

                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);

            }

            if (info.getClassName().toString().equals("android.widget.Button") && info.getText().toString().equals(QQ_SEND)) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if (info.getText() != null && info.getText().toString().contains(QQ_NOTIFIED)) {

                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);

            }

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    private boolean searchNotificationAndClick(){
        if (this.rootNodeInfo == null) return false;

        List<AccessibilityNodeInfo> temp = this.findNodeInfo(this.rootNodeInfo, new String[]{
                QQ_NOTIFIED
        });

        if(!temp.isEmpty()){
            if (getRootInActiveWindow() != null) recycle(getRootInActiveWindow());
            return true;

        }
        return false;
    }

    private void searchHongBao(){
        if (this.rootNodeInfo== null) return;

        List<AccessibilityNodeInfo> temp = this.findNodeInfo(this.rootNodeInfo, new String[]{
            QQ_NORMAL_OPEN, QQ_PASSWD_OPEN, QQ_PASSWD_COPY
        });

        if(!temp.isEmpty()){
            String nodeID = Integer.toHexString(System.identityHashCode(this.rootNodeInfo));
            if (!nodeID.equals(lastID)){
                this.hongBaoFound = true;
                this.nodeInfos = temp;
                this.lastID = nodeID;
            }
        }
    }

    private String getHongbaoText(AccessibilityNodeInfo node) {
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException npe) {
            return null;
        }

        return content;
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
