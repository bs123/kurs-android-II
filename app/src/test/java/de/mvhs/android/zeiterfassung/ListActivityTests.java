package de.mvhs.android.zeiterfassung;

import android.support.v4.app.FragmentManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by eugen on 05.01.17.
 */

@RunWith(MockitoJUnitRunner.class)
public class ListActivityTests {
  // Mocks
  @Mock
  ListDataActivity _sut;

  @Mock
  FragmentManager _fragmentManager;

  @Mock
  EditFragment _detailFragment;

  @Test
  public void canEditOverSelection_ReturnTrue_IfNoDetailFragment(){
    // Arrange
    ListDataActivity sut = new ListDataActivity();

    // Act
    boolean result = sut.canEditOverSelection();

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void canEditOverSelection_ReturnTrue_IfDetailFragmentNotAdded(){
    // Arrange
    when(_fragmentManager.findFragmentById(R.id.DetailFragment))
        .thenReturn(_detailFragment);
    when(_sut.getSupportFragmentManager())
        .thenReturn(_fragmentManager);
    when(_sut.canEditOverSelection())
        .thenCallRealMethod();

    // Act
    boolean result = _sut.canEditOverSelection();

    // Assert
    assertThat(result, is(true));
  }
//
//  @Test
//  public void canEditOverSelection_ReturnFalse_IfDetailFragmentAdded(){
//    // Arrange
//    when(_fragmentManager.findFragmentById(R.id.DetailFragment))
//        .thenReturn(_detailFragment);
//    ListDataActivity sut = new ListDataActivity();
//
//    // Act
//    boolean result = sut.canEditOverSelection();
//
//    // Assert
//    assertThat(result, is(false));
//  }
}
