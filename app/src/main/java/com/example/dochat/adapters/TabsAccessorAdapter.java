package com.example.dochat.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.dochat.fragments.ChatsFragment;
import com.example.dochat.fragments.ContactsFragment;
import com.example.dochat.fragments.GroupsFragment;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {

        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }
}
