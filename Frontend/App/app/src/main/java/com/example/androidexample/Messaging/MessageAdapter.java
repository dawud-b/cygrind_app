package com.example.androidexample.Messaging;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private static final int VIEW_TYPE_CURRENT_USER = 1;
    private static final int VIEW_TYPE_OTHER_USER = 2;
    private OnReactionClickListener reactionClickListener;
    private Set<Reaction> unlockedPremiumReactions;

    public interface OnReactionClickListener {
        void onReactionClicked(Long messageId, String reactionType);
    }

    public MessageAdapter(List<Message> messages, OnReactionClickListener listener, Set<Reaction> unlockedPremiumReactions) {
        this.messages = messages;
        this.reactionClickListener = listener;
        this.unlockedPremiumReactions = unlockedPremiumReactions;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isCurrentUser ? VIEW_TYPE_CURRENT_USER : VIEW_TYPE_OTHER_USER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CURRENT_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_current_user, parent, false);
            return new CurrentUserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_other_user, parent, false);
            return new OtherUserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        List<Reaction> reactions = message.getReactions();
        String time = new SimpleDateFormat("HH:mm").format(message.getSent());

        if (holder instanceof CurrentUserViewHolder) {
            CurrentUserViewHolder h = (CurrentUserViewHolder) holder;
            h.messageText.setText(message.getContent());
            h.timestampText.setText(time);
            h.usernameText.setText(message.getSender().getUsername());

            resetReactionViews(h); // hide all reaction views

            if (reactions != null) {
                for (Reaction reaction : reactions) {
                    showReactionView(h, reaction);
                }
            }

        } else if (holder instanceof OtherUserViewHolder) {
            OtherUserViewHolder h = (OtherUserViewHolder) holder;
            h.messageText.setText(message.getContent());
            h.timestampText.setText(time);
            h.usernameText.setText(message.getSender().getUsername());

            resetReactionViews(h);

            if (reactions != null) {
                for (Reaction reaction : reactions) {
                    showReactionView(h, reaction);
                }
            }

            h.reactionContainer.setVisibility(View.GONE);

            holder.itemView.setOnLongClickListener(v -> {
                h.reactionContainer.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> h.reactionContainer.setVisibility(View.GONE), 3000);
                return true;
            });

            setReactionClickHandler(h.reactionThumbsUp, message.getId(), Reaction.LIKED, "LIKED");
            setReactionClickHandler(h.reactionLove, message.getId(), Reaction.LOVED, "LOVED");
            setReactionClickHandler(h.reactionStrong, message.getId(), Reaction.STRONG, "STRONG");
            setReactionClickHandler(h.reactionCool, message.getId(), Reaction.COOL, "COOL");
            setReactionClickHandler(h.reactionDisliked, message.getId(), Reaction.DISLIKED, "DISLIKED");
            setReactionClickHandler(h.reactionFire, message.getId(), Reaction.FIRE, "FIRE");
            setReactionClickHandler(h.reactionCrown, message.getId(), Reaction.CROWN, "CROWN");
            setReactionClickHandler(h.reactionTrophy, message.getId(), Reaction.TROPHY, "TROPHY");
            setReactionClickHandler(h.reactionDiamond, message.getId(), Reaction.DIAMOND, "DIAMOND");
            setReactionClickHandler(h.reactionUnicorn, message.getId(), Reaction.UNICORN, "UNICORN");
            setReactionClickHandler(h.reactionRocket, message.getId(), Reaction.ROCKET, "ROCKET");

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // === REACTION HANDLERS ===

    private void showReactionView(RecyclerView.ViewHolder holder, Reaction reaction) {
        ImageButton view = getReactionViewForType(holder, reaction.getReactionType());
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void resetReactionViews(CurrentUserViewHolder h) {
        h.reactionViewThumbsUp.setVisibility(View.GONE);
        h.reactionViewLove.setVisibility(View.GONE);
        h.reactionViewStrong.setVisibility(View.GONE);
        h.reactionViewCool.setVisibility(View.GONE);
        h.reactionViewDisliked.setVisibility(View.GONE);
        h.reactionViewFire.setVisibility(View.GONE);
        h.reactionViewCrown.setVisibility(View.GONE);
        h.reactionViewTrophy.setVisibility(View.GONE);
        h.reactionViewDiamond.setVisibility(View.GONE);
        h.reactionViewUnicorn.setVisibility(View.GONE);
        h.reactionViewRocket.setVisibility(View.GONE);
    }

    private void resetReactionViews(OtherUserViewHolder h) {
        h.reactionViewThumbsUp.setVisibility(View.GONE);
        h.reactionViewLove.setVisibility(View.GONE);
        h.reactionViewStrong.setVisibility(View.GONE);
        h.reactionViewCool.setVisibility(View.GONE);
        h.reactionViewDisliked.setVisibility(View.GONE);
        h.reactionViewFire.setVisibility(View.GONE);
        h.reactionViewCrown.setVisibility(View.GONE);
        h.reactionViewTrophy.setVisibility(View.GONE);
        h.reactionViewDiamond.setVisibility(View.GONE);
        h.reactionViewUnicorn.setVisibility(View.GONE);
        h.reactionViewRocket.setVisibility(View.GONE);
    }

    private ImageButton getReactionViewForType(RecyclerView.ViewHolder holder, int type) {
        if (holder instanceof CurrentUserViewHolder) {
            switch (type) {
                case Reaction.LIKED: return ((CurrentUserViewHolder) holder).reactionViewThumbsUp;
                case Reaction.LOVED: return ((CurrentUserViewHolder) holder).reactionViewLove;
                case Reaction.STRONG: return ((CurrentUserViewHolder) holder).reactionViewStrong;
                case Reaction.COOL: return ((CurrentUserViewHolder) holder).reactionViewCool;
                case Reaction.DISLIKED: return ((CurrentUserViewHolder) holder).reactionViewDisliked;
                case Reaction.FIRE: return ((CurrentUserViewHolder) holder).reactionViewFire;
                case Reaction.TROPHY: return ((CurrentUserViewHolder) holder).reactionViewTrophy;
                case Reaction.CROWN: return ((CurrentUserViewHolder) holder).reactionViewCrown;
                case Reaction.ROCKET: return ((CurrentUserViewHolder) holder).reactionViewRocket;
                case Reaction.DIAMOND: return ((CurrentUserViewHolder) holder).reactionViewDiamond;
                case Reaction.UNICORN: return ((CurrentUserViewHolder) holder).reactionViewUnicorn;
            }
        } else if (holder instanceof OtherUserViewHolder) {
            switch (type) {
                case Reaction.LIKED: return ((OtherUserViewHolder) holder).reactionViewThumbsUp;
                case Reaction.LOVED: return ((OtherUserViewHolder) holder).reactionViewLove;
                case Reaction.STRONG: return ((OtherUserViewHolder) holder).reactionViewStrong;
                case Reaction.COOL: return ((OtherUserViewHolder) holder).reactionViewCool;
                case Reaction.DISLIKED: return ((OtherUserViewHolder) holder).reactionViewDisliked;
                case Reaction.FIRE: return ((OtherUserViewHolder) holder).reactionViewFire;
                case Reaction.TROPHY: return ((OtherUserViewHolder) holder).reactionViewTrophy;
                case Reaction.CROWN: return ((OtherUserViewHolder) holder).reactionViewCrown;
                case Reaction.ROCKET: return ((OtherUserViewHolder) holder).reactionViewRocket;
                case Reaction.DIAMOND: return ((OtherUserViewHolder) holder).reactionViewDiamond;
                case Reaction.UNICORN: return ((OtherUserViewHolder) holder).reactionViewUnicorn;
            }
        }
        return null;
    }

    private void setReactionClickHandler(ImageButton button, Long messageId, int reactionType, String reactionName) {
        boolean isUnlocked = !isPremiumReaction(reactionType)
                || unlockedPremiumReactions.stream().anyMatch(r -> r.getReactionType() == reactionType);

        button.setAlpha(isUnlocked ? 1.0f : 0.4f);

        button.setOnClickListener(v -> {
            if (isUnlocked) {
                if (reactionClickListener != null) {
                    reactionClickListener.onReactionClicked(messageId, reactionName);
                }
            } else {
                Toast.makeText(v.getContext(), "This is a premium reaction. Unlock it to use!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPremiumReaction(long type) {
        return type > 6;
    }

    // === VIEWHOLDERS ===

    public static class CurrentUserViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, usernameText, timestampText;

        ImageButton reactionViewLove, reactionViewThumbsUp, reactionViewStrong, reactionViewCool, reactionViewDisliked;
        ImageButton reactionViewFire,  reactionViewTrophy;
        ImageButton reactionViewCrown, reactionViewDiamond, reactionViewRocket, reactionViewUnicorn;

        public CurrentUserViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_content);
            usernameText = itemView.findViewById(R.id.message_username);
            timestampText = itemView.findViewById(R.id.message_timestamp);

            reactionViewThumbsUp = itemView.findViewById(R.id.reactionViewThumbsUp);
            reactionViewLove = itemView.findViewById(R.id.reactionViewLove);
            reactionViewStrong = itemView.findViewById(R.id.reactionViewStrong);
            reactionViewCool = itemView.findViewById(R.id.reactionViewCool);
            reactionViewDisliked = itemView.findViewById(R.id.reactionViewDisliked);

            reactionViewFire = itemView.findViewById(R.id.reactionViewFire);
            reactionViewTrophy = itemView.findViewById(R.id.reactionViewTrophy);
            reactionViewCrown = itemView.findViewById(R.id.reactionViewCrown);
            reactionViewDiamond = itemView.findViewById(R.id.reactionViewDiamond);
            reactionViewRocket = itemView.findViewById(R.id.reactionViewRocket);
            reactionViewUnicorn = itemView.findViewById(R.id.reactionViewUnicorn);
        }
    }


    public static class OtherUserViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, usernameText, timestampText;
        LinearLayout reactionContainer;

        ImageButton reactionLove, reactionThumbsUp, reactionStrong, reactionCool, reactionDisliked;
        ImageButton reactionViewLove, reactionViewThumbsUp, reactionViewStrong, reactionViewCool, reactionViewDisliked;

        ImageButton reactionViewFire, reactionViewMindBlown, reactionViewFlexing, reactionViewTrophy;
        ImageButton reactionViewCrown, reactionViewDiamond, reactionViewRocket, reactionViewUnicorn;
        ImageButton reactionFire, reactionTrophy, reactionCrown, reactionDiamond, reactionRocket, reactionUnicorn;

        public OtherUserViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_content);
            usernameText = itemView.findViewById(R.id.message_username);
            timestampText = itemView.findViewById(R.id.message_timestamp);

            reactionContainer = itemView.findViewById(R.id.reactionContainer);
            reactionThumbsUp = itemView.findViewById(R.id.reactionThumbsUp);
            reactionLove = itemView.findViewById(R.id.reactionLove);
            reactionStrong = itemView.findViewById(R.id.reactionStrong);
            reactionCool = itemView.findViewById(R.id.reactionCool);
            reactionDisliked = itemView.findViewById(R.id.reactionDisliked);

            reactionFire= itemView.findViewById(R.id.reactionFire);
            reactionCrown= itemView.findViewById(R.id.reactionCrown);
            reactionRocket= itemView.findViewById(R.id.reactionRocket);
            reactionTrophy= itemView.findViewById(R.id.reactionTrophy);
            reactionUnicorn = itemView.findViewById(R.id.reactionUnicorn);
            reactionDiamond = itemView.findViewById(R.id.reactionDiamond);

            reactionViewThumbsUp = itemView.findViewById(R.id.reactionViewThumbsUp);
            reactionViewLove = itemView.findViewById(R.id.reactionViewLove);
            reactionViewStrong = itemView.findViewById(R.id.reactionViewStrong);
            reactionViewCool = itemView.findViewById(R.id.reactionViewCool);
            reactionViewDisliked = itemView.findViewById(R.id.reactionViewDisliked);

            reactionViewFire = itemView.findViewById(R.id.reactionViewFire);
            reactionViewTrophy = itemView.findViewById(R.id.reactionViewTrophy);
            reactionViewCrown = itemView.findViewById(R.id.reactionViewCrown);
            reactionViewDiamond = itemView.findViewById(R.id.reactionViewDiamond);
            reactionViewRocket = itemView.findViewById(R.id.reactionViewRocket);
            reactionViewUnicorn = itemView.findViewById(R.id.reactionViewUnicorn);
        }
    }



}
