package org.bille;

import org.apache.maven.surefire.shared.compress.utils.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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

    public Integer getMax() {
        return Collections.max(membersToMembers.keySet().stream()
                .map(MemberData::getCards).collect(Collectors.toList()));
    }

    public boolean check() {

        return membersToMembers.keySet()
                .stream()
                .noneMatch(sender -> membersToMembers.get(sender).size() != sender.getCards())
                && membersFromMembers.keySet()
                .stream()
                .noneMatch(receiver -> membersFromMembers.get(receiver).size() != receiver.getCards());
    }

    public int getTotalCards() {
        return membersToMembers.keySet().stream().mapToInt(MemberData::getCards).sum();
    }

    public TreeMap<Integer, HashSet<String>> getOverview() {
        TreeMap<Integer, HashSet<String>> overview = new TreeMap<>();
        membersToMembers.keySet().forEach(memberData -> {
            if (overview.containsKey(memberData.getCards())) {
                overview.get(memberData.getCards()).add(memberData.getUserName());
            } else {
                overview.put(memberData.getCards(), Sets.newHashSet(memberData.getUserName()));
            }
        });
        return overview;
    }

    public List<String> getUsernames() {
        return membersFromMembers.keySet().stream()
                .filter(x -> !x.hasNoCards())
                .map(MemberData::getUserName)
                .sorted()
                .collect(Collectors.toList());
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
