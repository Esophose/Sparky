package dev.esophose.discordbot.cards;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

public class Deck {

    private List<Card> cards;

    public Deck() {
        this.cards = Lists.newArrayList(Card.values());
        Collections.shuffle(this.cards);
    }

    public Card draw() {
        if (this.cards.isEmpty())
            this.reset();
        return this.cards.remove(0);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(this.cards);
    }

    public void reset() {
        this.cards = new Deck().getCards();
    }

}
