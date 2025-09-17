package tecna.oldwalkinganimation;

import net.minecraft.text.Text;

public class SCHelper {

    @FunctionalInterface
    public interface TextCreator {
        Text create(String content);
    }

    public static final TextCreator text;

    static {
        //? if >=1.17 {
            text = Text::literal;
        //?} else {
            /*text = LiteralText::new;
        *///?}
    }

}
