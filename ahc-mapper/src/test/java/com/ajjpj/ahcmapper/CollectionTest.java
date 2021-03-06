package com.ajjpj.ahcmapper;

import com.ajjpj.ahcmapper.builder.AhcMapperBuilder;
import com.ajjpj.ahcmapper.builder.AhcMapperEqualsProviderBuilder;
import com.ajjpj.ahcmapper.builder.AhcMapperEqualsProviderExtension;
import com.ajjpj.ahcmapper.builder.AhcMapperListStrategy;
import com.ajjpj.ahcmapper.classes.ClassA;
import com.ajjpj.ahcmapper.classes.ClassB;
import com.ajjpj.ahcmapper.classes.InnerClassA;
import com.ajjpj.ahcmapper.classes.InnerClassB;
import com.ajjpj.ahcmapper.core.equivalence.AhcMapperEquivalenceStrategy;
import com.ajjpj.ahcmapper.core.equivalence.equals.EqualsBasedEquivalenceStrategy;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ajjpj.ahcmapper.builder.AhcMapperBuilder.newObjectMapping;


public class CollectionTest extends Assert {
    @Test
    public void testSet() throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
        .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
        .build();

        final Set<ClassA> set = new HashSet<ClassA>();
        final ClassA a1 = new ClassA();
        a1.setFirstName("A");
        set.add(a1);
        final ClassA a2 = new ClassA();
        a2.setFirstName("B");
        set.add(a2);
        
        @SuppressWarnings("unchecked")
        final Set<ClassA> mapped = mapper.map(set, Set.class, ClassA.class, null, Set.class, ClassA.class);
        
        assertEquals(2, mapped.size());
    }

    @Test
    public void testSetFromArray() throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
        .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
        .build();
        
        final ClassA a1 = new ClassA();
        final ClassA a2 = new ClassA();
        final ClassA[] arr = new ClassA[] {a1, a2, a1};
        a1.setFirstName("A");
        a2.setFirstName("B");
        
        @SuppressWarnings("unchecked")
        final Set<ClassA> mapped = mapper.map(arr, arr.getClass(), ClassA.class, null, Set.class, ClassA.class);
        
        assertEquals(2, mapped.size());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testToObjectArray () throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
        .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
        .build();

        final ClassA[] objArray = new ClassA[] {new ClassA(), new ClassA()};

        final ClassA[] fromArray = mapper.map (objArray, objArray.getClass(), ClassA.class, null, objArray.getClass(), ClassA.class);
        assertEquals (2, fromArray.length);
        assertTrue (fromArray[0].getClass() == ClassA.class);
        assertTrue (fromArray[1].getClass() == ClassA.class);

        assertNotSame (fromArray[0], objArray[0]);
        assertNotSame (fromArray[1], objArray[1]);

        final ClassA[] fromNotNullArray = (ClassA[]) mapper.map (objArray, objArray.getClass(), ClassA.class, 
                new ClassA[1], (Class) objArray.getClass(), ClassA.class);
        assertEquals (2, fromNotNullArray.length);
        assertTrue (fromNotNullArray[0].getClass() == ClassA.class);
        assertTrue (fromNotNullArray[1].getClass() == ClassA.class);

        assertNotSame (fromNotNullArray[0], objArray[0]);
        assertNotSame (fromNotNullArray[1], objArray[1]);

        final ClassA[] fromList = mapper.map (Arrays.asList(objArray), List.class, ClassA.class, null, objArray.getClass(), ClassA.class);
        assertEquals (2, fromList.length);
        assertTrue (fromList[0].getClass() == ClassA.class);
        assertTrue (fromList[1].getClass() == ClassA.class);
        
        assertNotSame (fromList[0], objArray[0]);
        assertNotSame (fromList[1], objArray[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testListFromObjectArray () throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
            .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
            .build();

        final Object[] objArray = new ClassA[] {new ClassA(), new ClassA()};
        
        final List<ClassA> asList = mapper.map (objArray, objArray.getClass(), ClassA.class, null, List.class, ClassA.class);
        assertEquals (2, asList.size());
        assertTrue (asList.get(0).getClass() == ClassA.class);
        assertTrue (asList.get(1).getClass() == ClassA.class);
        
        assertNotSame (asList.get(0), objArray[0]);
        assertNotSame (asList.get(1), objArray[1]);
    }

    @Test
    public void testMergeAndOrphansInCollection () throws Exception {
        final AhcMapperEqualsProviderExtension equalsExtension = new AhcMapperEqualsProviderExtension() {
            @Override
            public boolean canHandle(Class<?> sourceClass, Class<?> targetClass) {
                return targetClass == InnerClassB.class;
            }

            @Override
            public CompareStrategy getCompareStrategy(Class<?> cls) throws Exception {
                return NATURAL_COMPARE_STRATEGY;
            }
            
            @Override
            public Object createEqualsPlaceholder(Object source, Class<?> sourceClass, Class<?> targetClass) throws Exception {
                return new InnerClassB (((InnerClassA) source).getPhone(), null); 
            }
        };
        
        final AhcMapperEquivalenceStrategy equivalenceStrategy = new EqualsBasedEquivalenceStrategy(new AhcMapperEqualsProviderBuilder().withExtension(equalsExtension).build());
        
        final AhcMapper mapper = new AhcMapperBuilder ()
            .withEquivalenceStrategy(equivalenceStrategy)
            .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassB.class))
            .withBidirectionalMapping(newObjectMapping(InnerClassA.class, InnerClassB.class))
            .build();
        
        final ClassA a = new ClassA ();
        a.getPhone().add (new InnerClassA ("123", "new"));
        a.getPhone().add (new InnerClassA ("456", "new"));
        
        final InnerClassB innerB1 = new InnerClassB ("456", "old");
        final InnerClassB innerB2 = new InnerClassB ("789", "old");
        final ClassB b = new ClassB ();
        b.getPhone().add (innerB1);
        b.getPhone().add (innerB2);
        
        final ClassB mappedB = mapper.map (a, b);
        
        assertSame (mappedB, b);
        
        assertEquals (2, b.getPhone().size());
        assertEquals ("123", b.getPhone().get(0).getPhone());
        assertEquals ("new", b.getPhone().get(0).getOther());
        assertEquals ("456", b.getPhone().get(1).getPhone());
        assertEquals ("new", b.getPhone().get(1).getOther());
        
        assertSame (innerB1, b.getPhone().get(1));
        assertNotSame (innerB2, b.getPhone().get (0));
        assertEquals ("789", innerB2.getPhone());
    }
    
    @Test
    public void testListAsList() throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
        .withListMapping(AhcMapperListStrategy.LIST)
        .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
        .build();

        final List<ClassA> source1 = Arrays.asList(new ClassA(), new ClassA());

        @SuppressWarnings("unchecked")
        final List<ClassA> target1 = mapper.map (source1, List.class, ClassA.class, null, List.class, ClassA.class);
        assertEquals (2, target1.size());
        assertTrue (target1.get(0).getClass() == ClassA.class);
        assertTrue (target1.get(1).getClass() == ClassA.class);

        assertNotSame (target1.get(0), target1.get(1));

        final ClassA a = new ClassA();
        final List<ClassA> source2 = Arrays.asList(a, a);
        
        @SuppressWarnings("unchecked")
        final List<ClassA> target2 = mapper.map (source2, List.class, ClassA.class, null, List.class, ClassA.class);
        assertEquals (2, target2.size());
        assertTrue (target2.get(0).getClass() == ClassA.class);
        assertTrue (target2.get(1).getClass() == ClassA.class);
        
        assertSame (target2.get(0), target2.get(1));
    }

    @Test
    public void testListAsSet() throws Exception {
        final AhcMapper mapper = new AhcMapperBuilder()
            .withListMapping(AhcMapperListStrategy.SET)
            .withBidirectionalMapping(newObjectMapping(ClassA.class, ClassA.class))
            .build();

        final List<ClassA> source1 = Arrays.asList(new ClassA(), new ClassA());

        @SuppressWarnings("unchecked")
        final List<ClassA> target1 = mapper.map (source1, List.class, ClassA.class, null, List.class, ClassA.class);
        assertEquals (2, target1.size());
        assertTrue (target1.get(0).getClass() == ClassA.class);
        assertTrue (target1.get(1).getClass() == ClassA.class);

        assertNotSame (target1.get(0), target1.get(1));

        final ClassA a = new ClassA();
        final List<ClassA> source2 = Arrays.asList(a, a);

        @SuppressWarnings("unchecked")
        final List<ClassA> target2 = mapper.map (source2, List.class, ClassA.class, null, List.class, ClassA.class);
        assertEquals (1, target2.size());
        assertTrue (target2.get(0).getClass() == ClassA.class);
    }
}
