package de.mvhs.android.zeiterfassung;

import android.support.v4.app.Fragment;
import  android.support.v4.app.FragmentManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by javadev on 11.01.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListActiviesTests {
    // Mocks
    @Mock
    FragmentManager _fragementManager;

    @Mock
    EditFragment _editFragment;

    @Mock
    ListDataActivity _sut;

    @Test
    public void canEditOverSelection_Return_true_ifNoDetailFragment() {
        //arrange
        ListDataActivity sut = new ListDataActivity();

        //act
        boolean result = sut.canEditOverSelection();

        //assert
        assertTrue(result);

    }

    @Test
    public void canEditOverSelection_ReturnTrue_IfDetailFragmentNotAdded() {
        //arrange
        when(_fragementManager.findFragmentById(R.id.DetailFragment))
                .thenReturn(_editFragment);

        when(_sut.getSupportFragmentManager()).thenReturn(_fragementManager);

        when(_sut.canEditOverSelection()).thenCallRealMethod();

      //  ListDataActivity sut = new ListDataActivity();

        // act
        boolean result = _sut.canEditOverSelection();

        //assert
        assertTrue(result);

    }


//    @RunWith(PowerMockRunner.class)
  //  @PrepareForTest(EditFragment.class)

    @Test
    public void canEditOverSelection_ReturnFalse_IfdetailFragemtIsAdded() {
        when(_fragementManager.findFragmentById(R.id.DetailFragment)).thenReturn(_editFragment);
        when(_sut.getSupportFragmentManager()).thenReturn(_fragementManager);
        when(_sut.canEditOverSelection()).thenCallRealMethod();
        when(_editFragment.isAdded()).thenReturn(true);


        // act
        boolean result = _sut.canEditOverSelection();

        //assert
        assertFalse(result);

    }
}
