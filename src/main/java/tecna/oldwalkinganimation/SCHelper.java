package tecna.oldwalkinganimation;

//? if >=26.1 || neoforge {
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.text.Text;
*///?}

public class SCHelper {

    @FunctionalInterface
    public interface TextCreator {
        //? if >=26.1 || neoforge {
        Component create(String content);
        //?} else {
        /*Text create(String content);
        *///?}
    }

    public static final TextCreator text;

    static {
        //? if >=26.1 || neoforge {
        text = Component::literal;
        //?} else if >=1.17 {
        /*text = Text::literal;
        *///?} else {
            /*text = LiteralText::new;
        *///?}
    }

}
