package org.bille;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FehlkaufRound {

    private final TreeMap<MemberData, List<MemberData>> membersToMembers;
    private final TreeMap<MemberData, List<MemberData>> membersFromMembers;

    public FehlkaufRound(TreeMap<MemberData, List<MemberData>> membersToMembers) {
        this.membersToMembers = membersToMembers;
        this.membersFromMembers = initMembersFromMembers(membersToMembers);
    }

    public TreeMap<MemberData, List<MemberData>> getSenders() {
        return membersToMembers;
    }

    public TreeMap<MemberData, List<MemberData>> getReceivers() {
        return membersFromMembers;
    }

    public boolean check() {

        return membersToMembers.keySet()
                .stream()
                .noneMatch(sender -> membersToMembers.get(sender).size() != sender.getCards())
                && membersFromMembers.keySet()
                .stream()
                .noneMatch(receiver -> membersFromMembers.get(receiver).size() != receiver.getCards());
    }

    private static @NotNull TreeMap<MemberData, List<MemberData>> initMembersFromMembers(Map<MemberData, List<MemberData>> membersToMembers) {
        TreeMap<MemberData, List<MemberData>> membersFromMembers = new TreeMap<>();
        membersToMembers.keySet().forEach(member -> {
            membersFromMembers.put(member, new ArrayList<>());
            membersToMembers.keySet()
                    .stream()
                    .filter(other -> membersToMembers.get(other).contains(member))
                    .forEach(other -> membersFromMembers.get(member).add(other));
        });
        membersFromMembers.keySet().stream().map(membersFromMembers::get).forEach(Collections::sort);
        return membersFromMembers;
    }

}
