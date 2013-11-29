package audio;

import java.applet.Applet;
import java.applet.AudioClip;

public class SoundEffect {
    public static final SoundEffect ABILITY_UNLOCK = new SoundEffect("SFX_AbilityUnlock.wav");
    public static final SoundEffect BLIP = new SoundEffect("SFX_Blip.wav");
    public static final SoundEffect CLEAR_GRID = new SoundEffect("SFX_ClearGrid.wav");
    public static final SoundEffect GAME_OVER = new SoundEffect("SFX_GameOver.wav");
    public static final SoundEffect GAME_START = new SoundEffect("SFX_GameStart.wav");
    public static final SoundEffect LEVEL_UP = new SoundEffect("SFX_LevelUp.wav");
    public static final SoundEffect LINE_PIECE = new SoundEffect("SFX_LinePiece.wav");
    public static final SoundEffect HARD_DROP = new SoundEffect("SFX_PieceHardDrop.wav");
    public static final SoundEffect STORE = new SoundEffect("SFX_PieceHold.wav");
    public static final SoundEffect ROTATE_FAIL = new SoundEffect("SFX_PieceRotateFail.wav");
    public static final SoundEffect ROTATE = new SoundEffect("SFX_PieceRotateLR.wav");
    public static final SoundEffect SOFT_DROP = new SoundEffect("SFX_PieceSoftDrop.wav");
    public static final SoundEffect SCORE_BOOST = new SoundEffect("SFX_ScoreBoost.wav");
    public static final SoundEffect CLEAR_DOUBLE = new SoundEffect("SFX_SpecialLineClearDouble.wav");
    public static final SoundEffect CLEAR_SINGLE = new SoundEffect("SFX_SpecialLineClearSingle.wav");
    public static final SoundEffect CLEAR_TRIPLE = new SoundEffect("SFX_SpecialLineClearTriple.wav");
    public static final SoundEffect ZERO_GRAVITY = new SoundEffect("SFX_ZeroGravity.wav");

    private AudioClip mClip;

    private SoundEffect(String filename) {        
        mClip = Applet.newAudioClip(getClass()
                .getResource("/resources/sound_effects/" + filename));
    }
    
    public void play() {
        try {
            new Thread() {
                public void run() {
                    mClip.play();
                }
            }.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
