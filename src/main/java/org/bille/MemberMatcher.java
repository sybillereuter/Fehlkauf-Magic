package org.bille;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MemberMatcher {

    final List<MemberData> members;
    private int initialMax;
    private int currentMax;
    final int reduction;

    public MemberMatcher (List<MemberData> members) {

        this(members, 1);
    }

    public MemberMatcher (List<MemberData> members, int reduction) {

        this.reduction = reduction;
        this.members = members;
        computeMax();
        members.sort(Comparator.comparing(MemberData::getCards).reversed());
    }

    public FehlkaufRound match() throws ArrayIndexOutOfBoundsException {

        TreeMap<MemberData, Integer> membersByReceivedCards = getMembersByReceivedCards();
        TreeMap<MemberData, List<MemberData>> matchedMembers = new TreeMap<>();

        // todo ne "fehlkaufexception" wenn member max ist aber n anderer mehr karten hat

        for (MemberData member : members) {
            int cards = member.getCards();
            List<MemberData> options = getOptions(membersByReceivedCards, member);
            List<MemberData> matches = new ArrayList<>();
            if (member.isMax() && options.size() < cards) {
                return null;
            }
            try {
                IntStream.range(0, cards).mapToObj(options::get).forEach(receiver -> {
                matches.add(receiver);
                membersByReceivedCards.put(receiver, membersByReceivedCards.get(receiver) + 1);
                });
            } catch (IndexOutOfBoundsException e) {
                // todo: das kann ewig passieren wenn zu große zahl gewünscht ist!
                return null;
            }
            matchedMembers.put(member, matches.stream().sorted().collect(Collectors.toList()));
        }
        return new FehlkaufRound(matchedMembers);
    }

    private void computeMax() {
        int initialMax = Collections.max(members.stream()
                .filter(memberData -> !memberData.isMax())
                .map(MemberData::getCards).collect(Collectors.toList()));
        this.initialMax = initialMax;
        int maxMembers = (int) members.stream().filter(MemberData::isMax).count();
        int optimalMax = initialMax + maxMembers - reduction;
        this.currentMax = optimalMax;
        members.stream().filter(MemberData::isMax).forEach(member -> member.setCards(optimalMax));
    }

    @NotNull
    private TreeMap<MemberData, Integer> getMembersByReceivedCards() {
        return members.stream()
                .collect(Collectors.toMap(other -> other, others -> 0, (a, b) -> b, TreeMap::new));
    }

    private List<MemberData> getOptions(TreeMap<MemberData, Integer> received, MemberData member) {

        return received.entrySet().stream()
                .filter(memberData -> memberData.getKey().getCards() > received.get(memberData.getKey()))
                .filter(memberData -> !memberData.getKey().equals(member))
                .sorted(OptionsComparator::doCompare)
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public int getInitialMax() {
        return initialMax;
    }

    public int getCurrentMax() {
        return currentMax;
    }

    private static class OptionsComparator implements Comparator<Map.Entry<MemberData, Integer>> {

        public static int doCompare(Map.Entry<MemberData, Integer> a, Map.Entry<MemberData, Integer> b) {
            int cmp1 = a.getValue().compareTo(b.getValue());
            if (cmp1 != 0) {
                return cmp1;
            } else {

                int y = a.getKey().getCards();
                int x  = b.getKey().getCards();
                return Integer.compare(x, y);
            }
        }

        @Override
        public int compare(Map.Entry<MemberData, Integer> o1, Map.Entry<MemberData, Integer> o2) {
            return 0;
        }
    }
}
