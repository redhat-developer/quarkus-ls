package toremove;

import java.util.ArrayList;
import java.util.List;

public class Item {

	private List<Review> reviews = new ArrayList<>();
	private final String name;

	public Item(int i) {
		this.name = "Item: " + i;
	}

	public void addReview(Review review) {
		getReviews().add(review);
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public String getName() {
		return name;
	}
}
