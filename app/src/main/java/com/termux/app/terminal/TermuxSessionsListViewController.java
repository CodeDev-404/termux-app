package com.termux.app.terminal;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.droidshell.app.R;
import com.termux.app.TermuxActivity;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;
import com.termux.shared.theme.NightMode;
import com.termux.shared.theme.ThemeUtils;
import com.termux.terminal.TerminalSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TermuxSessionsListViewController extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int VIEW_TYPE_GROUP = 0;
    private static final int VIEW_TYPE_SESSION = 1;

    private static final String GROUP_HOME = "Home";

    final TermuxActivity mActivity;

    final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

    /** The live session list from {@link com.termux.app.TermuxService#getTermuxSessions()}. */
    private final List<TermuxSession> mSessionList;

    /** Rows to display, either a {@link String} group header or a {@link TermuxSession}. */
    private final List<Object> mRows = new ArrayList<>();

    /** Maps each session row to its ordinal number (1-based) ignoring group headers. */
    private final Map<TermuxSession, Integer> mSessionOrdinals = new LinkedHashMap<>();

    public TermuxSessionsListViewController(TermuxActivity activity, List<TermuxSession> sessionList) {
        this.mActivity = activity;
        this.mSessionList = sessionList;
        buildRows();
    }

    private void buildRows() {
        mRows.clear();
        mSessionOrdinals.clear();

        if (!mActivity.getProperties().isSessionDrawerGroupingEnabled()) {
            int sessionOrdinal = 0;
            for (TermuxSession termuxSession : mSessionList) {
                mRows.add(termuxSession);
                mSessionOrdinals.put(termuxSession, ++sessionOrdinal);
            }
            return;
        }

        // Group sessions by the first path component of their working directory.
        LinkedHashMap<String, List<TermuxSession>> groups = new LinkedHashMap<>();
        for (TermuxSession termuxSession : mSessionList) {
            String groupName = getGroupName(termuxSession.getTerminalSession());
            List<TermuxSession> groupSessions = groups.get(groupName);
            if (groupSessions == null) {
                groupSessions = new ArrayList<>();
                groups.put(groupName, groupSessions);
            }
            groupSessions.add(termuxSession);
        }

        int sessionOrdinal = 0;
        for (Map.Entry<String, List<TermuxSession>> entry : groups.entrySet()) {
            mRows.add(entry.getKey());
            for (TermuxSession termuxSession : entry.getValue()) {
                mRows.add(termuxSession);
                mSessionOrdinals.put(termuxSession, ++sessionOrdinal);
            }
        }
    }

    private static String getGroupName(TerminalSession session) {
        if (session == null) return "null";
        String cwd = session.getCwd();
        if (TextUtils.isEmpty(cwd)) return GROUP_HOME;

        if (cwd.startsWith(TermuxConstants.TERMUX_HOME_DIR_PATH)) {
            String relativePath = cwd.substring(TermuxConstants.TERMUX_HOME_DIR_PATH.length());
            // Remove leading "/".
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
            if (TextUtils.isEmpty(relativePath)) return GROUP_HOME;
            int separatorIndex = relativePath.indexOf('/');
            return separatorIndex == -1 ? relativePath : relativePath.substring(0, separatorIndex);
        } else {
            // Working directory outside of home, group by the first path component.
            int separatorIndex = cwd.indexOf('/', 1);
            return separatorIndex == -1 ? cwd : cwd.substring(0, separatorIndex);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) instanceof String ? VIEW_TYPE_GROUP : VIEW_TYPE_SESSION;
    }

    @Override
    public int getCount() {
        return mRows.size();
    }

    @Override
    public Object getItem(int position) {
        return mRows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        buildRows();
        super.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (getItemViewType(position) == VIEW_TYPE_GROUP) {
            return getGroupView((String) mRows.get(position), convertView, parent);
        } else {
            return getSessionView((TermuxSession) mRows.get(position), convertView, parent);
        }
    }

    @NonNull
    private View getGroupView(String groupName, View convertView, @NonNull ViewGroup parent) {
        View groupRowView = convertView;
        if (groupRowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            groupRowView = inflater.inflate(R.layout.item_terminal_sessions_list_group, parent, false);
        }

        TextView groupTitleView = groupRowView.findViewById(R.id.session_group_title);
        groupTitleView.setText(groupName);

        return groupRowView;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    private View getSessionView(TermuxSession termuxSession, View convertView, @NonNull ViewGroup parent) {
        View sessionRowView = convertView;
        if (sessionRowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            sessionRowView = inflater.inflate(R.layout.item_terminal_sessions_list, parent, false);
        }

        TextView sessionTitleView = sessionRowView.findViewById(R.id.session_title);
        TextView sessionSubtitleView = sessionRowView.findViewById(R.id.session_subtitle);
        View sessionStatusDot = sessionRowView.findViewById(R.id.session_status_dot);

        TerminalSession sessionAtRow = termuxSession.getTerminalSession();
        if (sessionAtRow == null) {
            sessionTitleView.setText("null session");
            return sessionRowView;
        }

        String name = sessionAtRow.mSessionName;
        String sessionTitle = sessionAtRow.getTitle();

        String numberPart = "[" + mSessionOrdinals.get(termuxSession) + "] ";
        String sessionNamePart = (TextUtils.isEmpty(name) ? "" : name);
        String sessionTitlePart = (TextUtils.isEmpty(sessionTitle) ? "" : ((sessionNamePart.isEmpty() ? "" : " ") + sessionTitle));

        String fullSessionTitle = numberPart + sessionNamePart;
        SpannableString fullSessionTitleStyled = new SpannableString(fullSessionTitle);
        fullSessionTitleStyled.setSpan(boldSpan, 0, fullSessionTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        sessionTitleView.setText(fullSessionTitleStyled);
        sessionSubtitleView.setText(sessionTitlePart.trim());

        boolean sessionRunning = sessionAtRow.isRunning();

        int defaultColor = ThemeUtils.shouldEnableDarkTheme(mActivity, NightMode.getAppNightMode().getName()) ? Color.WHITE : Color.BLACK;
        int color = sessionRunning || sessionAtRow.getExitStatus() == 0 ? defaultColor : Color.RED;
        sessionTitleView.setTextColor(color);

        if (sessionStatusDot != null) {
            int dotColor = sessionRunning
                ? ContextCompat.getColor(mActivity, R.color.session_status_dot_running)
                : (sessionAtRow.getExitStatus() == 0 ? Color.GRAY : Color.RED);
            sessionStatusDot.setBackgroundTintList(ColorStateList.valueOf(dotColor));
        }
        return sessionRowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getItemViewType(position) != VIEW_TYPE_SESSION) return;
        TermuxSession clickedSession = (TermuxSession) getItem(position);
        mActivity.getTermuxTerminalSessionClient().setCurrentSession(clickedSession.getTerminalSession());
        mActivity.getDrawer().closeDrawers();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (getItemViewType(position) != VIEW_TYPE_SESSION) return false;
        final TermuxSession selectedSession = (TermuxSession) getItem(position);
        mActivity.getTermuxTerminalSessionClient().renameSession(selectedSession.getTerminalSession());
        return true;
    }

}
