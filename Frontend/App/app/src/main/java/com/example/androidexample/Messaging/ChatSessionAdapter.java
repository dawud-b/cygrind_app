package com.example.androidexample.Messaging;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * The ChatSessionAdapter is a custom adapter for displaying a list of chat sessions (message threads)
 * in a RecyclerView. Each item represents a message thread with details such as the group name,
 * the last message sent in the thread, and the timestamp of the last message. The adapter is responsible
 * for inflating the layout for each item, binding the data to the views, and handling item click events.
 * <p>
 * This adapter utilizes a view holder pattern to efficiently manage the views for each chat session item.
 * The adapter also provides an interface for handling click events on individual message threads.
 */
public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.MessageThreadViewHolder> {

    private List<ChatSession> threads;  // List of chat sessions (message threads)
    private OnMessageThreadClickListener listener;  // Listener interface for handling thread clicks

    /**
     * Constructor to initialize the adapter with the list of chat sessions and the click listener.
     *
     * @param threads The list of chat sessions (message threads) to be displayed in the RecyclerView.
     * @param listener The listener for handling click events on message threads.
     */
    public ChatSessionAdapter(List<ChatSession> threads, OnMessageThreadClickListener listener) {
        this.threads = threads;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder to hold the view for a message thread item.
     * This method is called when a new item view needs to be created.
     *
     * @param parent The parent view group (RecyclerView) to which the item view will be added.
     * @param viewType The type of view to create (not used in this implementation).
     * @return A new ViewHolder for the item.
     */
    @Override
    public MessageThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_thread, parent, false);
        return new MessageThreadViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder. This method is called for each item in the RecyclerView to display the data.
     *
     * @param holder The ViewHolder for the item.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(MessageThreadViewHolder holder, int position) {
        ChatSession thread = threads.get(position);  // Get the chat session at the current position

        Message lastMessage = thread.getLastMessage();  // Get the last message in the thread

        if (lastMessage != null) {
            if (lastMessage.getSent() != null) {
                // Format the timestamp of the last message
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = sdf.format(lastMessage.getSent());
                holder.timestampTextView.setText(time);
            }
            holder.lastMessageTextView.setText(lastMessage.getContent());
        }

        // Safely set the group name (default to "Unknown Group" if null)
        holder.groupNameTextView.setText(thread.getGroupName() != null ? thread.getGroupName() : "Unknown Group");

        // Set up the click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageThreadClick(thread);  // Trigger the callback method when the item is clicked
            }
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The number of chat sessions in the adapter.
     */
    @Override
    public int getItemCount() {
        return threads != null ? threads.size() : 0;  // Check if threads list is null
    }

    /**
     * ViewHolder class for holding the views associated with each chat session item.
     */
    public static class MessageThreadViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;  // TextView for the group name
        TextView lastMessageTextView;  // TextView for the last message content
        TextView timestampTextView;  // TextView for the timestamp of the last message

        /**
         * Constructor to initialize the ViewHolder with the item view.
         *
         * @param itemView The view for a single chat session item.
         */
        public MessageThreadViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.item_convo_name);
            lastMessageTextView = itemView.findViewById(R.id.textViewLastMessage);
            timestampTextView = itemView.findViewById(R.id.textViewLastMessageTime);
        }
    }

    /**
     * Interface for handling clicks on message thread items.
     * The implementing class must define the behavior for when a message thread is clicked.
     */
    public interface OnMessageThreadClickListener {
        /**
         * Called when a message thread is clicked.
         *
         * @param thread The chat session (message thread) that was clicked.
         */
        void onMessageThreadClick(ChatSession thread);
    }
}
